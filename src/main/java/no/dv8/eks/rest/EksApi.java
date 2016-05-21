package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.EksAlps;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.forms.FormHelper;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XFunction;
import no.dv8.utils.Forker;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;

@Slf4j
public class EksApi implements XFunction<Exchange, Exchange> {

    final EksResources resources;

    public EksApi(EksResources resources) {
        Objects.requireNonNull(resources);
        this.resources = resources;
    }

    static html error404(String path) {
        return new html().add(new body().add(new h1("404: " + path)));
    }

    EksForms forms() {
        return new EksForms(resources);
    }

    EksIndex index() {
        return new EksIndex(resources);
    }

    @Override
    public Exchange apply(Exchange exchange) throws IOException {

        PrintWriter writer = exchange.res.getWriter();
        exchange.res.setContentType("text/html");

        String path = new URL(exchange.req.getRequestURL().toString()).getPath();

        final Element<?> obj;
        String title = path;
        String method = exchange.req.getMethod();
        ResourcePaths urls = resources.urlCreator;

//        Forker<Exchange, Object> forker = new Forker<Exchange, Object>()
//          .add( "alps", x -> x.req.getPathInfo().equals( "/alps" ), this::alps)
//          .add( "get-index", x -> urls.isRoot( x.req.getPathInfo() ), x -> new EksIndex(resources).index() )
//          .add( "get-item", x -> urls.isItem( x.getFullPath() ), x -> "hei" );
//
//        if( true ) {
//            Object apply = forker.apply(exchange);
////            obj = resources.toElement(apply);
//            obj = new p(apply.toString());
//        } else
        if (path.equals("alps")) {
            obj = new XHTMLSerialize<>().generateElement(new EksAlps().alps(), 100);
            title = "ALPS";
        } else if (urls.isRoot(path)) {
            obj = index().index();
        } else if (urls.isItem(path)) {
            String itemClass = urls.type(path);
            String itemId = urls.id(path);
            Resource<?> resource = resources.locateByName(itemClass).get();
            Object item = resource.locator().apply(itemId).get();
            switch (method.toUpperCase()) {
                case "GET":
                    obj = resources.toElement(item);
                    break;
                case "POST":
                case "PUT":
                    obj = resources.executeUpdate(resource, item, exchange.req);
                    break;
                default:
                    throw new UnsupportedOperationException(method);
            }
        } else if (urls.isEditForm(path)) {
            String itemClass = urls.type(path);
            String itemId = urls.id(path);
            Resource<?> resource = resources.locateByName(itemClass).get();
            Object item = resource.locator().apply(itemId).get();
            obj = forms().editForm(resource.updater(), item);
        } else if (urls.isQueryForm(path)) {
            FormHelper fh = new FormHelper(this.resources);
            obj = fh.searchForm(urls.queryName(path));
        } else if (urls.isQueryResult(path)) {
            Collection<?> objects = resources.executeQuery(urls.queryName(path), exchange.req);
            ul ul = new ul();
            objects.forEach( o -> ul.add( new li( linkToObject(o))));
            obj = ul;
        } else if (urls.isCreateForm(path)) {
            String itemClass = urls.type(path);
            obj = forms().createForm(itemClass);
        } else if (urls.isCreateResult(path)) {
            String itemClass = urls.type(path);
            Resource r = resources.locateByName(itemClass).get();
            obj = forms().executeCreate(r, exchange.req);
        } else {
            obj = error404(path);
        }
        exchange.res.setCharacterEncoding("utf-8");

        writer.print(EksHTML.complete(obj, title).toString());
        writer.close();
        return exchange;
    }

    private Object alps(Exchange x) {
        return new XHTMLSerialize<>().generateElement(new EksAlps().alps(), 100);
    }

    private a linkToObject(Object u) {
        return new a(u.toString()).href(resources.urlCreator.viewItem(resources.itemClass(u), resources.itemId(u)));
    }


}
