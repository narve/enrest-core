package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.xhtml.generation.elements.fieldset;
import no.dv8.xhtml.generation.elements.form;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.legend;
import no.dv8.xhtml.generation.support.Element;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toList;

public class QueryFormHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final ResourceRegistry resources;

    public QueryFormHandler(ResourceRegistry resources) {
        this.resources = resources;
    }


    @Override
    public boolean test(Exchange x) {
        return resources.getPaths().isQueryForm(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        String rel = resources.getPaths().queryName(exchange.getFullPath());
        QueryResource q = resources.queryForRel(rel);
        List<Element<?>> controls = q.params()
          .stream()
          .map(p -> new input().type(p.getHtmlType()).name(p.getName()).id(p.getName()))
          .collect(toList());
        return exchange.withEntity(new form()
          .clz(rel)
          .get()
          .action(resources.getPaths().queryResult(rel))
          .add(
            new fieldset()
              .add(new legend(rel.toString()))
              .add(controls)
              .add(new input().submit().value(rel))
          ));
    }
}
