package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.model.Article;
import no.dv8.eks.semantic.EksAlps;
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
import static no.dv8.enrest.mutation.FormHelper.pathToCreateResult;
import static no.dv8.enrest.mutation.FormHelper.pathToCreators;

@Slf4j
public class EksApi {

    public final String basePath;

    public EksApi(String basePath) {
        this.basePath = basePath;
    }

    EksResources eksResources() {
        return new EksResources(basePath);
    }

    EksQueries queries() {
        return new EksQueries(basePath);
    }

    EksForms forms() {
        return new EksForms(basePath);
    }

    EksIndex index() {
        return new EksIndex(basePath);
    }


    public XBiConsumer<HttpServletRequest, HttpServletResponse> api() {

        return (req, res ) -> {

            Function<HttpServletRequest, Map<String, String>> reqToValues =
              r -> new Props().single(r.getParameterMap());

            Function<Map<String, String>, Article> toObj = m -> new Props().createAndSetProps(Article.class, m);
            Function<Article, Article> saver = a -> a;
            Function<Article, String> serializer = a -> a.toString();

            BiConsumer<HttpServletResponse, String> sender = null;


            Function<HttpServletRequest, String> reqToString = reqToValues
              .andThen(toObj)
              .andThen(saver)
              .andThen(serializer);

//        sender.accept( res, reqToString.apply(req) );


            Function<HttpServletRequest, Article> reqProcessor = hr -> new Article();

//        Function<Article, HttpServletRequest> serializer =


            Function<Article, Long> al = Article::getId;
            Function<Long, String> ls = String::valueOf;


            Function<Article, String> f1 = al.andThen(ls);
            Function<Article, String> f2 = ls.compose(al);


            Function<HttpServletRequest, HttpServletResponse> func;

            BiConsumer<HttpServletRequest, HttpServletRequest> bic;


//        asList( req )
//          .stream()
//          .map( r -> r.getParameterMap())
//          .map( )

//        ServletOutputStream o = res.getOutputStream();
            PrintWriter writer = res.getWriter();
            res.setContentType("text/html");

            writer.write("<!DOCTYPE html>\n");

            String path = new URL( req.getRequestURL().toString() ).getPath();
            log.info("FULLPATH: {}", path);
//            path = path.substring(ServletBase.length());
            path = path.substring(basePath.length());
            log.info("PATH2: {}", path);
            if (path.startsWith("/")) path = path.substring(1);
            if (path.endsWith("/")) path = path.substring(0, path.length()-1);
            log.info("PATH3: {}", path);

            Element<?> obj;
            String title = path;

            String method = req.getMethod();

            if (path.equals("alps")) {
                obj = new XHTMLSerialize<>().generateElement(new EksAlps().eks(), 100);
                title = "ALPS";
            } else if (path.isEmpty()) {
                obj = index().index();
            } else if (path.startsWith(pathToResource + "/") && method.equalsIgnoreCase("GET")) {
                obj = eksResources().itemToElement(path.substring(pathToResource.length() + 1));
            } else if (path.startsWith(pathToResource + "/") && (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("put"))) {
                obj = eksResources().executeUpdate(path.substring(pathToResource.length() + 1), req);
            } else if (path.startsWith(editPathToResource + "/")) {
                obj = forms().editForm(path.substring(editPathToResource.length() + 1));
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

    static html error404(String path) {
        return new html().add(new body().add(new h1("404: " + path)));
    }


}
