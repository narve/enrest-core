package no.dv8.enrest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.handlers.FileHandler;
import no.dv8.enrest.semantic.EksAlps;
import no.dv8.enrest.handlers.*;
import no.dv8.enrest.writers.JSONWriter;
import no.dv8.enrest.writers.XHTMLWriter;
import no.dv8.utils.FuncList;
import no.dv8.xhtml.generation.elements.p;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static no.dv8.utils.FuncList.always;
import static no.dv8.utils.FuncList.ifOutEntity;

@Slf4j
public class EnrestServlet extends HttpServlet {

    public ResourceRegistry registry;
    private ServletConfig config;
    public UnaryOperator<Exchange> handler;

    public EnrestServlet() {
    }

    public EnrestServlet(ResourceRegistry resourceRegistry) {
        this.registry = resourceRegistry;
        init(null);
    }

    public ResourceRegistry createResources() {
        if( this.registry != null )
            return registry;
        throw new UnsupportedOperationException("abstract getMethod: createResources. Override, or provide resources in constructor" );
    };

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) {
        handler.apply(new Exchange(req, res));
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }

    static UnaryOperator<Exchange> reqLogger() {
        return x -> {
            log.info("Start of: {} {}", x.getMethod(), x.getFullPath());
            return x;
        };
    }

    static UnaryOperator<Exchange> finisher() {
        return x -> {
            log.info("Done with {} {}", x.getMethod(), x.getFullPath());
            x.finish();
            return x;
        };
    }


    public static UnaryOperator<Exchange> mainFork(ResourceRegistry resources) {
        return new FuncList<Exchange>()
          .add("test", x -> x.getFullPath().endsWith("/test"), x -> x.withOutEntity(new p("test")))
          .add("static-files", x -> x.getPathInfo() != null && x.getPathInfo().startsWith("/_files" ), new FileHandler("/home/narve/dev", "/eks/_files"))
          .add("swagger", x -> x.getPathInfo() != null && x.getPathInfo().startsWith("/swagger.json" ), new SwaggerHandler(resources))
          .add(new EksAlps())
          .add(new IndexHandler(resources))
          .add(new ItemHandler(resources))
          .add(new EditFormHandler(resources))
          .add(new QueryFormHandler(resources))
          .add(new QueryResultHandler(resources))
          .add(new CreateFormHandler(resources))
          .add(new CreateResultHandler(resources))
          .add(new DeleteFormHandler(resources))
          .add(new DeleteByFormHandler(resources))
          .add(new NotFoundHandler())
          .forker(x -> "No suitable path-match for " + x);
    }


    @Override
    public void init(ServletConfig config) {
        this.config = config;
        handler = defaultChain(createResources());
    }

    public static UnaryOperator<Exchange> defaultChain(ResourceRegistry resources) {
        return new FuncList<Exchange>()
          .add("req-logger", always(), reqLogger())
          .add("req-logger", always(), cors())
          .add(new EntityParser(resources))
          .add("main", always(), mainFork(resources))
          .add("linker", ifOutEntity(), new LinkHandler(resources))
          .add("writer", ifOutEntity(), writer())
          .add("res-logger", always(), finisher())
          .all();
    }

    public static UnaryOperator<Exchange> writer() {
        return new FuncList<Exchange>()
          .add("html", isJSON(), new JSONWriter())
          .add("html", isXHTML(), new XHTMLWriter())
          .add("html", always(), new XHTMLWriter())
          .forker(x -> "No suitable outputter for " + x);
    }

    public static UnaryOperator<Exchange> cors() {
        return x -> x.withHeader( "Access-Control-Allow-Origin", "*" );
    }

    public static Predicate<Exchange> isXHTML() {
        return x -> {
            String acc = x.getHeader("Accept");
            log.info(x + ": " + acc);
            return acc != null && acc.contains("html");
        };
    }

    public static Predicate<Exchange> isJSON() {
        return x -> {
            if( x.getContentType() != null && x.getContentType().contains( "json" ) ) {
                return true;
            }
            String acc = x.getHeader("Accept");
            log.info(x + ": " + acc);
            return acc != null && acc.contains("json");
        };
    }

}
