package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.EksAlps;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XFunction;
import no.dv8.utils.Forker;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class EksApi implements XFunction<Exchange, Exchange> {

    final EksResources resources;
    private final ResourcePaths urls;

    public EksApi(EksResources resources) {
        Objects.requireNonNull(resources);
        this.resources = resources;
        this.urls = resources.urlCreator;
    }

    html error404(Exchange x) {
        return new html().add(new body().add(new h1("404: " + x.getFullPath())));
    }

    EksCreateForms forms() {
        return new EksCreateForms(resources);
    }

    @Override
    public Exchange apply(Exchange exchange) throws IOException {

        Function<Exchange, Element<?>> forker = new Forker<Exchange, Element<?>>()
          .add("alps", x -> x.req.getPathInfo().equals("/alps"), this::alps)
          .add("index", new EksIndex(this.resources))
          .add( "item", new EksItem( this.resources ) )
          .add("edit-form", x -> urls.isEditForm(x.getFullPath()), this::handleEditForm)
          .add("query-form", new EksQueryForms(resources))
          .add("query-result", x -> urls.isQueryResult(x.getFullPath()), this::handleQueryResult)
          .add("create-form", new EksCreateForms(resources))
          .add("create-result", x -> urls.isCreateResult(x.getFullPath()), this::executeCreate)
          .add("404", x -> true, this::error404)
          .forker();
        Element<?> result = forker.apply(exchange);
        String title = exchange.req.getPathInfo();

        exchange.res.setContentType("text/html");
        exchange.res.setCharacterEncoding("utf-8");
        PrintWriter writer = exchange.res.getWriter();
        writer.print(EksHTML.complete(result, title).toString());
        writer.close();
        return exchange;
    }


    private Element<?> executeCreate(Exchange exchange) {
        Element<?> obj;
        String itemClass = urls.type(exchange.getFullPath());
        Resource r = resources.locateByName(itemClass);
        obj = forms().executeCreate(r, exchange.req);
        return obj;
    }

    private Element<?> handleEditForm(Exchange exchange) {
        String itemClass = urls.type(exchange.getFullPath());
        String itemId = urls.id(exchange.getFullPath());
        Resource<?> resource = resources.locateByName(itemClass);
        Object item = resource.locator().apply(itemId).get();
        return forms().editForm(resource.updater(), item);
    }

    private Element<?> handleQueryResult(Exchange exchange) {
        Collection<?> objects = resources.executeQuery(this.resources.urlCreator.queryName(exchange.getFullPath()), exchange.req);
        ul ul = new ul();
        objects.forEach(o -> ul.add(new li().add(linkToObject(o))));
        return ul;
    }

    private Element<?> alps(Exchange x) {
        return new XHTMLSerialize<>().generateElement(new EksAlps().alps(), 100);
    }

    private a linkToObject(Object u) {
        return new a(u.toString()).href(resources.urlCreator.viewItem(resources.itemClass(u), resources.itemId(u)));
    }


}
