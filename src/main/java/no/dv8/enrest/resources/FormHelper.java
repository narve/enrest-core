package no.dv8.enrest.resources;

import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class FormHelper {


    public static String pathToCreators = "create-forms";
    public static String pathToCreateResult = "create-result";

    public static input text(Object r ) {
        return new input().text().id(r).name(r).placeholder(r.toString());
    }

    public static div textControl(Object r ) {
        return control( text(r), r );
    }

    public static div control(Element element, Object name ) {
        return new div()
          .add( new label().add( name.toString() ).forId( element.get("id").toString() ) )
          .add( element );
    }

    public static form createForm(Object name, List<Element<?>> inputs, String basePath, Object method ) {
        List<div> inputDivs = inputs.stream()
          .map( e -> control(e, e.get("name")))
          .collect( toList() );

        return new form()
          .clz(name)
          .method(method)
          .action( basePath + pathToCreateResult+"/" + name)
          .set( "accept-charset", "utf-8")
          .set( "enc-type", "application/x-www-form-urlencoded")
          .add(new fieldset()
            .add(new legend(name))
            .add( inputDivs )
            .add(new input().submit().value(name)
            ));
    }

}
