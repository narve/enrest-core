package no.dv8.eks.rest;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import no.dv8.dirs.Dirs;
import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.functions.XBiConsumer;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksServlet.ServletBase;
import static no.dv8.functions.ServletFunctions.consumer;
import static no.dv8.functions.XBiConsumer.hidex;

@javax.servlet.annotation.WebServlet(urlPatterns = {ServletBase + "/*"})
@Slf4j
public class EksServlet extends HttpServlet {

    public static final String ServletBase = "/eks";

    public static String notNull(String in) {
        return in == null ? "" : in;
    }

    List<Pair<Predicate<HttpServletRequest>, BiConsumer<HttpServletRequest, HttpServletResponse>>> consumers() {
        return asList(
          new Pair<>(
            r -> r.getPathInfo().startsWith("/_files"),
            hidex(new Dirs("/home/narve/", "_files"))
          ),
          new Pair<>(
            r -> r.getPathInfo().startsWith("/_test1"),
            hidex(new EksApi("").test1())
          ),
          new Pair<>(
            r -> r.getPathInfo().startsWith("/Comment"),
            hidex(new EksApi("").test3(Comment.class))
          ),
          new Pair<>(
            r -> r.getPathInfo().startsWith("/_test2"),
            hidex(new EksApi("").test2())
          ),
          new Pair<>(
            r -> r.getPathInfo().startsWith("/_user"),
            hidex(new UserResource().testBIC())
          ),
          new Pair<>(
            r -> r.getPathInfo().startsWith("/api"),
            hidex(new EksApi(ServletBase + "/api").api())
          )
        );
    }

    Function<HttpServletRequest, Article> testFunc() {
        return req -> new Article();
    }

    XBiConsumer<HttpServletRequest, HttpServletResponse> testBIC() {
        return consumer( testFunc() );
    }


    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        log.info("PathInfo: {}", req.getPathInfo());
        log.info("RequestURL: {}", req.getRequestURL());
        Optional<Pair<Predicate<HttpServletRequest>, BiConsumer<HttpServletRequest, HttpServletResponse>>>
          handler = consumers()
          .stream()
          .filter(p -> p.getKey().test(req))
          .findFirst();

        BiConsumer<HttpServletRequest, HttpServletResponse> logger = (a, b) -> {
            log.info("REQUEST: {}, mapped to: {}", req.getRequestURL(), handler.isPresent() ? handler.get().getValue() : null);
        };

        if (handler.isPresent()) {
            BiConsumer mod = logger.andThen(handler.get().getValue());
            mod.accept(req, res);
        } else {
            throw new NullPointerException("No handler for " + req.getPathInfo());
        }
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }


    @Override
    public void init(ServletConfig config) throws ServletException {
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    static class EntityResponse<T> {
        public HttpServletRequest req;
        public T entity;
    }
}
