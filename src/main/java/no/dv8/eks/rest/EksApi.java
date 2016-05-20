package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.model.Article;
import no.dv8.eks.semantic.EksAlps;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XBiConsumer;
import no.dv8.reflect.Props;
import no.dv8.xhtml.generation.elements.body;
import no.dv8.xhtml.generation.elements.h1;
import no.dv8.xhtml.generation.elements.html;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static no.dv8.functions.ServletFunctions.consumer;

@Slf4j
public class EksApi {

    final EksResources resources;

    Function<HttpServletRequest, Article> reqProcessor = hr -> new Article();
    Function<Article, Long> al = Article::getId;
    Function<Long, String> ls = String::valueOf;
    Function<Article, String> f1 = al.andThen(ls);
    Function<Article, String> f2 = ls.compose(al);
    Function<HttpServletRequest, HttpServletResponse> func;
    BiConsumer<HttpServletRequest, HttpServletRequest> bic;

    public EksApi(EksResources resources) {
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

    Function<HttpServletRequest, Map<String, String>> reqToMap() {
        return r -> new Props().single(r.getParameterMap());
    }

    <T> Function<Map<String, String>, T> mapToObj(Class<T> clz) {
        return m -> new Props().createAndSetProps(clz, m);
    }

    <T> Function<T, T> saver(Class<T> clz) {
        return a -> {
            CRUD<T> crud = new CRUD<>(clz);
            a = crud.insert(a);
            log.info("size: {}", crud.all().size());
            return a;
        };
    }

    <T> Function<T, String> serializer() {
        return Object::toString;
    }

    XBiConsumer<HttpServletResponse, String> sender() {
        return (r, s) -> {
            r.getWriter().print(s);
        };
    }

    public XBiConsumer<HttpServletRequest, HttpServletResponse> test1() {
        return
          (in, out) -> sender().accept(
            out, reqToMap()
              .andThen(mapToObj(Article.class))
              .andThen(a -> {
                  log.info("Saving: {}", a);
                  return a;
              })
              .andThen(saver(Article.class))
              .andThen(serializer())
              .apply(in)
          );
    }

    public XBiConsumer<HttpServletRequest, HttpServletResponse> test2() {
        return
          consumer(
            reqToMap()
              .andThen(mapToObj(Article.class))
              .andThen(a -> {
                  log.info("Saving: {}", a);
                  return a;
              })
              .andThen(saver(Article.class))
          );
    }

    public <T> XBiConsumer<HttpServletRequest, HttpServletResponse> test3(Class<T> clz) {
        return
          consumer(
            reqToMap()
              .andThen(mapToObj(clz))
              .andThen(a -> {
                  log.info("Saving: {}", a);
                  return a;
              })
              .andThen(saver(clz))
          );
    }

    public XBiConsumer<HttpServletRequest, HttpServletResponse> api() {

        return (req, res) -> {
            PrintWriter writer = res.getWriter();
            res.setContentType("text/html");

            String path = new URL(req.getRequestURL().toString()).getPath();
            path = path.substring(resources.basePath.length());
            if (path.startsWith("/")) path = path.substring(1);
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

            final Element<?> obj;
            String title = path;
            String method = req.getMethod();
            ResourcePaths urls = resources.urlCreator;

            if (path.equals("alps")) {
                obj = new XHTMLSerialize<>().generateElement(new EksAlps().alps(), 100);
                title = "ALPS";
            } else if (path.isEmpty()) {
                obj = index().index();
            } else if (urls.isItem(path) ) {
                String itemClass = urls.type( path );
                String itemId = urls.id( path );
                Resource<?> resource = resources.locateByName(itemClass).get();
                Object item = resource.locator().apply(itemId).get();
                switch( method.toUpperCase()) {
                    case "GET":
                        obj = resources.toElement(item);
                        break;
                    case "POST":
                    case "PUT":
                        obj = resources.executeUpdate(resource, item, req );
                        break;
                    default:
                        throw new UnsupportedOperationException(method);
                }
            } else if (urls.isEditForm(path)) {
                String itemClass = urls.type( path );
                String itemId = urls.id( path );
                Resource<?> resource = resources.locateByName(itemClass).get();
                Object item = resource.locator().apply(itemId).get();
                obj = forms().editForm(resource.updater(), item);
            } else if (urls.isQueryForm(path)) {
                obj = queries().searchForm(urls.queryName(path));
            } else if (urls.isQueryResult(path)) {
                obj = queries().executeQuery(urls.queryName(path), req);
            } else if (urls.isCreateForm(path)) {
                String itemClass = urls.type( path );
                obj = forms().createForm(itemClass);
            } else if (urls.isCreateResult(path)) {
                String itemClass = urls.type( path );
                Resource r = resources.locateByName(itemClass).get();
                obj = forms().executeCreate(r, req);
            } else {
                obj = error404(path);
            }
            res.setCharacterEncoding("utf-8");

            writer.print(EksHTML.complete(obj, title).toString());
            writer.close();
        };
    }


}
