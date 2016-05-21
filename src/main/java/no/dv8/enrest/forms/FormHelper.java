package no.dv8.enrest.forms;

import no.dv8.eks.rest.EksResources;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class FormHelper {

    final EksResources resources;

    public FormHelper(EksResources resources) {
        this.resources = resources;
    }

    public static input text(Object r ) {
        return new input().text().id(r).name(r).placeholder(r.toString());
    }

    public static div textControl(Object r ) {
        return control( text(r), r );
    }

    public static div control(Element element, Object name ) {
        return new div()
          .clz( "input")
          .add( new label().add( name.toString() ).forId( element.get("id").toString() ) )
          .add( element );
    }

    public form createForm(Object name, List<Element<?>> inputs, Object method ) {
        List<div> inputDivs = inputs.stream()
          .map( e -> control(e, e.get("name")))
          .collect( toList() );

        return new form()
          .clz(name)
          .method(method)
          .action( resources.urlCreator.createAction( name ))
          .set( "accept-charset", "utf-8")
          .set( "enc-type", "application/x-www-form-urlencoded")
          .add(new fieldset()
            .add(new legend(name))
            .add( inputDivs )
            .add(new input().submit().value(name)
            ));
    }


    public form searchForm(Object rel) {
        QueryResource q = resources.queryForRel(rel);
        List<Element<?>> controls = q.params()
          .stream()
          .map(p -> control(new input().type(p.getHtmlType()).name(p.getName()).id(p.getName()), p.getName()))
          .collect(toList());
        return new form()
          .clz(rel)
          .get()
          .action(resources.urlCreator.queryResult(rel))
          .add(
            new fieldset()
              .add(new legend(rel.toString()))
              .add(controls)
              .add(new input().submit().value(rel))
          );
    }
}
