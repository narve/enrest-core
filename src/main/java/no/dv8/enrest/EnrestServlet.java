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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static no.dv8.utils.FuncList.always;
import static no.dv8.utils.FuncList.ifEntity;

@Slf4j
public abstract class EnrestServlet extends HttpServlet {

    private ServletConfig config;
    private UnaryOperator<Exchange> handler;

    public abstract ResourceRegistry createResources();

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

    UnaryOperator<Exchange> reqLogger() {
        return x -> {
            log.info("Start of: {} {}", x.req.getMethod(), x.getFullPath());
            return x;
        };
    }

    UnaryOperator<Exchange> finisher() {
        return x -> {
            log.info("Done with {} {}", x.req.getMethod(), x.getFullPath());
            x.finish();
            return x;
        };
    }


    UnaryOperator<Exchange> mainFork(ResourceRegistry resources) {
        return new FuncList<Exchange>()
          .add("test", x -> x.getFullPath().endsWith("/test"), x -> x.withEntity(new p("test")))
          .add("static-files", x -> x.req.getPathInfo() != null && x.req.getPathInfo().startsWith("/_files" ), new FileHandler("/home/narve/dev", "/eks/_files"))
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
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        ResourceRegistry resources = createResources();
        handler = new FuncList<Exchange>()
          .add("req-logger", always(), reqLogger())
          .add(new EntityParser(resources))
          .add("main", always(), mainFork(resources))
          .add("linker", ifEntity(), new LinkHandler(resources))
          .add("writer", ifEntity(), writer())
          .add("res-logger", always(), finisher())
          .all();
    }

    UnaryOperator<Exchange> writer() {
        return new FuncList<Exchange>()
          .add("html", isJSON(), new JSONWriter())
          .add("html", isXHTML(), new XHTMLWriter())
          .add("html", always(), new XHTMLWriter())
          .forker(x -> "No suitable outputter for " + x);
    }

    Predicate<Exchange> isXHTML() {
        return x -> {
            String acc = x.req.getHeader("Accept");
            log.info(x + ": " + acc);
            return acc != null && acc.contains("html");
        };
    }

    Predicate<Exchange> isJSON() {
        return x -> {
            String acc = x.req.getHeader("Accept");
            log.info(x + ": " + acc);
            return acc != null && acc.contains("json");
        };
    }

}
