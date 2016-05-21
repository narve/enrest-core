package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.model.Article;
import no.dv8.eks.semantic.EksAlps;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XUnaryOperator;
import no.dv8.xhtml.generation.elements.body;
import no.dv8.xhtml.generation.elements.h1;
import no.dv8.xhtml.generation.elements.html;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public class EksApi implements XUnaryOperator<Exchange> {

    final EksResources resources;

    Function<HttpServletRequest, Article> reqProcessor = hr -> new Article();
    Function<Article, Long> al = Article::getId;
    Function<Long, String> ls = String::valueOf;
    Function<Article, String> f1 = al.andThen(ls);
    Function<Article, String> f2 = ls.compose(al);

    public EksApi(EksResources resources) {
        Objects.requireNonNull(resources);
        this.resources = resources;
    }

    static html error404(String path) {
        return new html().add(new body().add(new h1("404: " + path)));
    }

    EksQueries queries() {
        return new EksQueries(resources);
    }

    EksForms forms() {
        return new EksForms(resources);
    }

    EksIndex index() {
        return new EksIndex(resources);
    }

    @Override
    public Exchange apply(Exchange x) throws IOException {
        PrintWriter writer = x.res.getWriter();
        x.res.setContentType("text/html");

        String path = new URL(x.req.getRequestURL().toString()).getPath();
//            path = path.substring(EksApi.this.resources.urlCreator.basePath.length());

        final Element<?> obj;
        String title = path;
        String method = x.req.getMethod();
        ResourcePaths urls = resources.urlCreator;

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
                    obj = resources.executeUpdate(resource, item, x.req);
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
            obj = queries().searchForm(urls.queryName(path));
        } else if (urls.isQueryResult(path)) {
            obj = queries().executeQuery(urls.queryName(path), x.req);
        } else if (urls.isCreateForm(path)) {
            String itemClass = urls.type(path);
            obj = forms().createForm(itemClass);
        } else if (urls.isCreateResult(path)) {
            String itemClass = urls.type(path);
            Resource r = resources.locateByName(itemClass).get();
            obj = forms().executeCreate(r, x.req);
        } else {
            obj = error404(path);
        }
        x.res.setCharacterEncoding("utf-8");

        writer.print(EksHTML.complete(obj, title).toString());
        writer.close();
        return x;
    }

}
