package no.dv8.concerts;

import no.dv8.rest2.framework.EnrestServletBase;
import no.dv8.rest2.framework.Link;
import no.dv8.rest3.Enrest;
import no.dv8.rest3.EnrestResource;
import no.dv8.rest3.Linker;
import no.dv8.rest3.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
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

        r.setLinker( getLinker());

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
          .pathPattern( "concerts")
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

    Linker getLinker() {
        return new Linker() {
            @Override
            public <T> List<Link> linksFrom(EnrestResource res, T t ) {

                if( t instanceof Concert ) {
                    List<Link> links = new ArrayList<>();

                    List<EnrestResource> targets = getEnrest().getResources().stream()
                      .filter(r -> r.getTo().equals(t.getClass()))
                      .collect(toList());

                    for( EnrestResource trg: targets ) {
                        Link l = new Link();
                        l.setTarget(trg);
                        l.setRel( "unknown-rel");
                        l.getParameters().add(Parameter.builder().name("id").value(((Concert) t).getId()).build());
                        links.add(l);
                    }

//                    Link self = new Link();
//                    self.setRel( "self");
//                    links.add( self);
//
//                    Link performer = new Link();
//                    performer.setRel( "performer" );
//                    links.add(performer);
//                    l.setTarget( );

                    return links;
                }


                return new ArrayList<>();
            }
        };
    }

}
