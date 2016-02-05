package no.dv8.concerts;

import no.dv8.rest2.framework.EnrestServletBase;
import no.dv8.rest3.Enrest;

import java.util.Map;

import static no.dv8.rest3.EnrestConfigurator.getBodyAsString;
import static no.dv8.rest3.EnrestConfigurator.parseJSON;

@javax.servlet.annotation.WebServlet(urlPatterns = {"/api", "/api/*"})
public class ConcertServlet extends EnrestServletBase {


    Enrest enrest = configure(new Enrest());
    public Enrest getEnrest() {
        return enrest;
    }

    @Override
    public String getRootPath() {
        return "api";
    }


    public Enrest configure(Enrest r) {
        Program p = new Program();
        r.single(String.class, Concert.class)
          .method("GET")
          .pathPattern( "get-concert-by-query")
          .queryParam("id")
          .name("Get concert by id - Query")
          .reqParser(req -> req.getParameterMap().get("id")[0])
          .handler(p::getConcert)
          .buildAndRegister();

        r.single(String.class, Concert.class)
          .method("GET")
          .pathParam("id")
          .name("Get concert by id - Path")
          .reqParser(req -> ((Map<String, String>) req.getAttribute("path-param-map")).get("id"))
          .handler(p::getConcert)
          .buildAndRegister();

        r.collection(Void.class, Concert.class)
          .method("GET")
          .name("List concerts")
          .handler((x) -> p.getConcerts())
          .buildAndRegister();

        r.collection(Void.class, Concert.class)
          .method("GET")
          .name("List concerts - Generics")
          .handler((x) -> p.getConcerts())
          .buildAndRegister();

        r.single(Concert.class, Concert.class)
          .method("POST")
          .name("Insert concert")
          .reqParser(req -> parseJSON(Concert.class, getBodyAsString(req)))
          .jsonBodyParam()
          .handler(x -> p.addConcert(x))
          .buildAndRegister();

        return r;
    }

}
