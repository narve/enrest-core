package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.xhtml.generation.elements.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.enrest.semantic.Rels.*;

public class IndexHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final ResourceRegistry resources;

    public IndexHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    public static ul listOf(List<a> items) {
        return new ul().add(
          items
            .stream()
            .map(a -> new li().add(a))
            .collect(toList())
        );
    }

    public List<a> basicLinks() {
        return asList(
          new a("index").rel(index).href(resources.getPaths().root()),
          new a("self").rel(self).href(resources.getPaths().root()),
          new a("profile").rel(profile).href("http://alps.io/spec/alps-xhtml-profiles/")
        );
    }

    public List<a> queries() {
        return resources
          .queries()
          .stream()
          .map(q -> relToA(q.getRel(), resources.getPaths().query(q.getRel())))
          .collect(toList());
    }

    @Override
    public boolean test(Exchange exchange) {
        return resources.getPaths().isRoot(exchange.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        article d =
          new article()
            .add(
              new section()
                .add(new h1("Misc"))
                .add(listOf(basicLinks()).clz("links")
                ).add(
                new section()
                  .add(new h1("Queries"))
                  .add(listOf(queries()).clz("queries")))
            ).add(
            new section()
              .add(new h1("Creators"))
              .add(new CreateFormHandler(resources).creatorForms())
          );
        return exchange.withEntity(d);
    }
}
