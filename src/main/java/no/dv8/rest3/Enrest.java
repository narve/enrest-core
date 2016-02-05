package no.dv8.rest3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Data
public class Enrest {

    List<EnrestResource> resources = new ArrayList<>();

    Linker linker = new Linker() {
    };
    RelTypeProvider relTypeProvider = new RelTypeProvider() {
    };

    public <From, To> EnrestResourceBuilder<From, To> single(Class<From> from, Class<To> to) {
        return new EnrestResourceBuilder<From, To>(this, from, to);
    }

    ;

    public <From, To> EnrestResourceBuilder<From, Collection> collection(Class<From> from, Class<To> to) {
        return new EnrestResourceBuilder<From, Collection>(this, from, Collection.class);
    }

    ;

    public EnrestResource register(EnrestResource<?, ?> res) {
        resources.add(res);
        return res;
    }

    public List<Element> index() {
        return index(this::form);
    }

    List<Element> index(Function<EnrestResource, Element> f) {
        return full(list().stream().map(f).map(a -> new li().add(a)).collect(toList()));
    }

    public static List<Element> full(Collection<Element<?>> collect) {
        return asList(new div()
          .add( new ul().add( collect ) )
          .add( new script().type( "text/javascript").src( "//code.jquery.com/jquery-2.2.0.min.js"))
          .add( new script().src("../enrest.js") )
        );
    }


    private a ref(EnrestResource r) {
        return new a(r.getName()).href("/resource/" + r.getReference());
    }

    private Element<?> form(EnrestResource r) {
//        return new a(r.getTo().getSimpleName()).href(r.getPath()).rel(relTypeProvider.reltype(r.getTo()));
        List<Parameter> queryParams = r.getQueryParams();
        List<Parameter> pathParams = r.getPathParams();
        List<Element<?>> qparams = queryParams.stream().map(s -> new input().type("text").name(s.getName()).placeholder(s.getName())).collect(toList());
        List<Element<?>> pparams = pathParams.stream().map(s -> new input().type("text").placeholder(s.getName())).collect(toList());

        String formId = r.getReference() + "-form";
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

