package no.dv8.sampleapp.concerts;

import no.dv8.enrest.servlet.EnrestServletBase;
import no.dv8.enrest.model.Link;
import no.dv8.enrest.container.Enrest;
import no.dv8.enrest.EnrestResource;
import no.dv8.enrest.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.dv8.enrest.EnrestConfigurator.getBodyAsString;
import static no.dv8.enrest.EnrestConfigurator.parseJSON;

@javax.servlet.annotation.WebServlet(urlPatterns = {"/api", "/api/*"})
public class ConcertServlet extends EnrestServletBase {

    private enum EnrestEnum implements Supplier<Enrest> {
        ENREST( configure( new Enrest() ));
        final Enrest enrest;
        EnrestEnum( Enrest r )  {
            this.enrest = r;
        }
        public Enrest get() {
            return this.enrest;
        }
    }

    Enrest enrest = configure(new Enrest());

    public Enrest getEnrest() {
        return enrest;
    }

    @Override
    public String getRootPath() {
        return "api";
    }


    public static Enrest configure(Enrest r) {

//        r.setLinker( concertLinker());

        Program p = new Program();
        EnrestResource s1 = r.single(String.class, Concert.class)
          .method("GET")
          .pathPattern("get-concert-by-executeQuery")
          .queryParam("id")
          .name("Get concert by id - Query")
          .reqParser(req -> req.getParameterMap().get("id")[0])
          .handler(id -> asList(p.getConcert(id)))
          .linker(concertLinker())
          .buildAndRegister();

        EnrestResource s2 = r.single(String.class, Concert.class)
          .method("GET")
          .pathParam("id")
          .name("Get concert by id - Path")
          .reqParser(req -> ((Map<String, String>) req.getAttribute("path-param-map")).get("id"))
          .handler(id -> asList(p.getConcert(id)))
          .linker(concertLinker())
          .buildAndRegister();

        EnrestResource l1 = r.collection(Void.class, Concert.class)
          .method("GET")
          .pathPattern("concerts")
          .name("List concerts")
          .handler((x) -> p.getConcerts())
          .linker(linker(false))
          .buildAndRegister();

        EnrestResource l2 = r.collection(Void.class, Concert.class)
          .method("GET")
          .name("List concerts - Generics")
          .handler((x) -> p.getConcerts())
          .linker(linker(false))
          .buildAndRegister();

        EnrestResource i1 = r.single(Concert.class, Concert.class)
          .method("POST")
          .name("Insert concert")
          .reqParser(req -> parseJSON(Concert.class, getBodyAsString(req)))
          .jsonBodyParam()
          .handler(x -> asList(p.addConcert(x)))
          .linker(linker(false))
          .buildAndRegister();

        return r;
    }

    static Function<Concert, List<Link>> linker(boolean single) {
        return new Function<Concert, List<Link>>() {
            @Override
            public List<Link> apply(Concert t) {
                List<Link> links = new ArrayList<>();
                List<EnrestResource> resources = EnrestEnum.ENREST.get().getResources().stream()
                  .filter(r -> r.getTo().equals(t.getClass()))
                  .filter( r -> r.isSingle() == single )
                  .collect(toList());
                for (EnrestResource trg : resources) {
                    Link l = new Link();
                    l.setTarget(trg);
                    l.setRel("unknown-rel");
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

        };
    }

    static Function<Concert, List<Link>> concertLinker() {
        return new Function<Concert, List<Link>>() {
            @Override
            public List<Link> apply(Concert t) {
                List<Link> links = new ArrayList<>();

                List<EnrestResource> targets = EnrestEnum.ENREST.get().getResources().stream()
                  .filter(r -> r.getTo().equals(t.getClass()))
                  .collect(toList());

                for (EnrestResource trg : targets) {
                    Link l = new Link();
                    l.setTarget(trg);
                    l.setRel("unknown-rel");
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

        };
    }

}
