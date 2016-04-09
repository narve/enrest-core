package no.dv8.enrest.container;

import com.google.gson.GsonBuilder;
import lombok.Data;
import no.dv8.enrest.EnrestResource;
import no.dv8.enrest.EnrestResourceBuilder;
import no.dv8.enrest.model.Parameter;
import no.dv8.enrest.spi.RelTypeProvider;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Data
public class Enrest {

    List<EnrestResource> resources = new ArrayList<>();

//    Linker linker = new Linker() {
//    };
    RelTypeProvider relTypeProvider = new RelTypeProvider() {
    };

    public <From, To> EnrestResourceBuilder<From, To> single(Class<From> from, Class<To> to) {
        return new EnrestResourceBuilder<From, To>(this, true, from, to);
    }

    ;

    public <From, To> EnrestResourceBuilder<From, To> collection(Class<From> from, Class<To> to) {
        return new EnrestResourceBuilder<From, To>(this, false, from, to);
    }



    public EnrestResource register(EnrestResource<?, ?> res) {
        resources.add(res);
        return res;
    }

    public EnrestResource index() {
        return new EnrestResource(true, "GET", "/", Void.class, Element.class, (req) -> null, (f) -> getResources(), "Index",null, null, null, null, null );
    }

    public EnrestResource<Void, String> indexResource() {
        EnrestResource re = new EnrestResourceBuilder(this, true, Void.class, String.class)
          .method( "GET" )
          .handler( (v) -> index( r -> new li().add( r.getName())) )
          .build();
        return re;
    }

    public List<Element> index(Function<EnrestResource, Element> f) {
        return full(list().stream().map(f).map(a -> new li().add(a)).collect(toList()));
    }

    public static List<Element> full(Collection<Element<?>> collect) {
        return asList(new div()
          .add( new ul().add( collect ) )
          .add( new script().type( "text/javascript").src( "//code.jquery.com/jquery-2.2.0.min.js"))
          .add( new script().src("../enrest.js") )
        );
    }


//    public a ref(EnrestResource r) {
//        return new a(r.getRel()).href("/resource/" + r.getReference());
//    }

    public Element<?> resourceLink(EnrestResource r, Parameter... prefilledParams) {
//        String href =
        return new a(r.getName()).href( "_resource/" + r.getReference() );
    }

    public Element<?> form(EnrestResource r, Parameter ... prefilledParams) {
//        return new a(r.getTo().getSimpleName()).href(r.getPath()).rel(relTypeProvider.reltype(r.getTo()));
        List<Parameter> queryParams = r.getQueryParams();
        List<Parameter> pathParams = r.getPathParams();

        Map<String, Parameter> m = asList( prefilledParams ).stream()
          .collect( Collectors.toMap( Parameter::getName, Function.identity() ) );

        Function<Parameter, Parameter> replace = x -> m.getOrDefault( x.getName(), x );

        List<Element<?>> qparams = queryParams.stream().map(replace).map(s -> new input().type("text").name(s.getName()).placeholder(s.getName())).collect(toList());
        List<Element<?>> pparams = pathParams.stream().map(replace).map(s -> new input().type("text").placeholder(s.getName())).collect(toList());

        String formId = r.getReference() + "-createForm";
        String pathSpanId = r.getReference() + "-path";
        String basePathSpanId = r.getReference() + "-basepath";

        pparams.forEach( e -> e.set( "onchange", String.format( "enrest.updateSpanAndForm( this, '%s', '%s', '%s' )", basePathSpanId, pathSpanId, formId ) ) );

        List<Parameter> bodyParams = r.getBodyParams();
        List<Element<?>> bodyInputs = bodyParams.stream().map(pxx -> {
            try {
                GsonBuilder b = new GsonBuilder().serializeNulls().setPrettyPrinting();
                String json = b.create().toJson( r.getFrom().newInstance());
                return new textarea().name("body").add(json);
            } catch (IllegalAccessException |InstantiationException e) {
                throw new RuntimeException(e);
            }
        }).collect( toList() );


        String path = "/api/" + r.getPath();
        for( Parameter p: pathParams ) {
            path += "/{:" + p.getName() + "}";
        }
        return new form()
          .id( formId )
          .action(path)
          .method(r.getMethod())
          .add(
            new fieldset()
              .add(new legend(r.getName()))
              .add( new div().add( new span("Link to this resource: ").add( resourceLink(r))))
              .add(new div()
                .add( new label("Base path: " ) )
                .add( new span( path ).id( basePathSpanId ) )
              ).add(new div()
                .add( new label("Path: " ) )
                .add( new span( path ).id( pathSpanId )
              )).add( qparams )
              .add( pparams )
              .add( new div().add( bodyInputs ) )
              .add(
              new div().add(
                new input().type("submit").value("GO")
              ))
          );
//        return new a(r.getTo().getSimpleName()).href(r.getPath()).rel(relTypeProvider.reltype(r.getTo()));
    }

    public List<EnrestResource> list() {
        return new ArrayList<>(resources);
    }
}

