package no.dv8.eks.rest;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import no.dv8.dirs.Dirs;
import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XBiConsumer;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.dv8.eks.rest.EksServlet.ServletBase;
import static no.dv8.functions.ServletFunctions.*;
import static no.dv8.functions.XBiConsumer.hidex;

@javax.servlet.annotation.WebServlet(urlPatterns = {ServletBase + "/*"})
@Slf4j
public class EksServlet extends HttpServlet {

    public static final String ServletBase = "/eks";
    UnaryOperator<Exchange> reqLogger = returner(x -> log.info("FULL PATH: {}", x.getFullPath()));
    UnaryOperator<Exchange> finisher = returner(x -> { log.info( "Finishing " + x.getFullPath() ); x.finish(); });

    Predicate<HttpServletRequest> startsWith( String prefix ) {
        return new Predicate<HttpServletRequest>() {
            @Override
            public boolean test(HttpServletRequest req) {
                return req.getPathInfo().startsWith(prefix );
            }

            @Override
            public String toString() {
                return "startsWith " + prefix;
            }
        };
    }

    List<Pair<Predicate<HttpServletRequest>, BiConsumer<HttpServletRequest, HttpServletResponse>>> consumers() {
        EksResources resources = new EksResources(ServletBase + "/api");
        resources.resources().add( new UserResource() );
        resources.resources().add( new QuestionResource() );
        resources.resources().add( BasicResource.create(resources, Article.class) );
        resources.resources().add( new BasicResource(resources, Comment.class) );

        return asList(
          new Pair<>(
            r -> r.getPathInfo() == null,
            hidex( "Redirect", (req, res) -> res.sendRedirect(req.getRequestURI() + "/" ))
          ),
          new Pair<>(
            startsWith("/_files"),
            hidex(new Dirs("/home/narve/", "_files"))
          ),
          new Pair<>(
            startsWith("/_test1"),
            hidex(new EksApi(new EksResources("")).test1())
          ),
          new Pair<>(
            startsWith("/Comment"),
            hidex(new EksApi(new EksResources("")).test3(Comment.class))
          ),
          new Pair<>(
            startsWith("/_test2"),
            hidex(new EksApi(new EksResources("")).test2())
          ),
          new Pair<>(
            startsWith("/_user"),
            hidex(new UserResource().testBIC())
          ),
          new Pair<>(
            startsWith("/api"),
            hidex(new EksApi(resources).api())
          ),
          new Pair<>(
            startsWith("/"),
//            hidex((req, res) -> res.getWriter().write(CoreMatchers.startsWith("asdf").toString()) )
            hidex( listBIC() )
          )
        );
    }

    Function<HttpServletRequest, Article> testFunc() {
        return req -> new Article();
    }

    XBiConsumer<HttpServletRequest, HttpServletResponse> testBIC() {
        return consumer(testFunc());
    }

    XBiConsumer<HttpServletRequest, HttpServletResponse> listBIC() {
        return (req, res ) -> res.getWriter().write( EksHTML.complete( listConsumers(), "Index").toHTML() );
    }

    Element<?> listConsumers() {
        return new ul()
          .add(
          consumers()
            .stream()
            .map( p -> new li().add( p.getKey()+ " => " + p.getValue()))
            .collect( toList() )
        );
    }


    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (true) {
            unaries(req, res);
        } else {
            bics(req, res);
        }
    }

    void unaries(HttpServletRequest req, HttpServletResponse res) throws IOException {
        UnaryOperator<Exchange> main = x -> {
            try {
                bics(x.req, x.res );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return x;
        };

        List<UnaryOperator<Exchange>> ops = asList(
          reqLogger, main, finisher
        );

        Exchange exchange = new Exchange(req, res);
        UnaryOperator<Exchange> composite = ops
          .stream()
          .reduce(UnaryOperator.identity(), (a, b) -> s -> b.apply(a.apply(s)));
        composite.apply(exchange);
    }


    void bics(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Optional<Pair<Predicate<HttpServletRequest>, BiConsumer<HttpServletRequest, HttpServletResponse>>> handler =
          consumers()
            .stream()
            .filter(p -> exMeansFalse(p.getKey()).test(req))
            .findFirst();

//        BiConsumer<HttpServletRequest, HttpServletResponse> reqLogger = (a, b) -> {
//            log.info("REQUEST: {}, mapped to: {}", req.getRequestURL(), handler.isPresent() ? handler.get().getValue() : null);
//        };
//            BiConsumer mod = reqLogger.andThen(handler.get().getValue());

        if (handler.isPresent()) {
            handler.get().getValue().accept(req, res);
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
