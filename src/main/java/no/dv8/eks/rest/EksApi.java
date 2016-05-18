package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.model.Article;
import no.dv8.eks.semantic.EksAlps;
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

import static no.dv8.eks.rest.EksQueries.pathToQueries;
import static no.dv8.eks.rest.EksQueries.pathToQueryResult;
import static no.dv8.eks.rest.EksResources.editPathToResource;
import static no.dv8.eks.rest.EksResources.pathToResource;
import static no.dv8.enrest.resources.FormHelper.pathToCreateResult;
import static no.dv8.enrest.resources.FormHelper.pathToCreators;
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

//            writer.write("<!DOCTYPE html>\n");

            String path = new URL(req.getRequestURL().toString()).getPath();
            log.info("FULLPATH: {}", path);
//            path = path.substring(ServletBase.length());
            path = path.substring(resources.basePath.length());
            log.info("PATH2: {}", path);
            if (path.startsWith("/")) path = path.substring(1);
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            log.info("PATH3: {}", path);

            Element<?> obj;
            String title = path;

            String method = req.getMethod();

            if (path.equals("alps")) {
                obj = new XHTMLSerialize<>().generateElement(new EksAlps().alps(), 100);
                title = "ALPS";
            } else if (path.isEmpty()) {
                obj = index().index();
            } else if (path.startsWith(pathToResource + "/") && method.equalsIgnoreCase("GET")) {
//                obj = eksResources().itemToElement(path.substring(pathToResource.length() + 1));
                String sub = path.substring(pathToResource.length() + 1);
                String itemClass = sub.split("/")[0];
                String itemId = sub.split("/")[1];
                Object item = resources.locateByName(itemClass).get().locator().apply(itemId).get();
                obj = resources.toElement(item);
            } else if (path.startsWith(pathToResource + "/") && (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("put"))) {
                obj = resources.executeUpdate(path.substring(pathToResource.length() + 1), req);
            } else if (path.startsWith(editPathToResource + "/")) {
                String sub = path.substring(pathToResource.length() + 1);
                String itemClass = sub.split("/")[0];
                String itemId = sub.split("/")[1];
                Object item = resources.locateByName(itemClass).get().locator().apply(itemId).get();
                obj = forms().editForm(item, path.substring(editPathToResource.length() + 1));
            } else if (path.startsWith(pathToQueries + "/")) {
                obj = queries().searchForm(path.substring(pathToQueries.length() + 1));
            } else if (path.startsWith(pathToQueryResult + "/")) {
                obj = queries().executeQuery(path.substring(pathToQueryResult.length() + 1), req);
            } else if (path.startsWith(pathToCreators + "/")) {
                obj = forms().createForm(path.substring(pathToCreators.length() + 1));
            } else if (path.startsWith(pathToCreateResult + "/")) {
                obj = forms().executeCreate(path.substring(pathToCreateResult.length() + 1), req);
            } else {
                obj = error404(path);
            }
            res.setCharacterEncoding("utf-8");

            writer.print(EksHTML.complete(obj, title).toString());
            writer.close();
        };
    }


}
