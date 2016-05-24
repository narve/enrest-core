package no.dv8.eks.rest;

import no.dv8.enrest.Exchange;
import no.dv8.functions.XFunction;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.eks.semantic.Rels.*;

public class EksIndex implements Predicate<Exchange>, XFunction<Exchange, Element<?>> {

    final EksResources resources;

    public EksIndex(EksResources resources) {
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
          new a("index").rel(index).href(resources.urlCreator.root()),
          new a("self").rel(self).href(resources.urlCreator.root()),
          new a("profile").rel(profile).href("http://alps.io/spec/alps-xhtml-profiles/")
        );
    }

    public List<a> queries() {
        return resources
          .queries()
          .stream()
          .map(q -> relToA(q.getRel(), resources.urlCreator.query(q.getRel())))
          .collect(toList());
    }

    @Override
    public boolean test(Exchange exchange) {
        return resources.urlCreator.isRoot(exchange.getFullPath());
    }

    @Override
    public Element<?> apply(Exchange exchange) {
        return
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
              .add(new EksForms(resources).creatorForms())
          );
    }
}
