package no.dv8.eks.rest;

import no.dv8.enrest.Exchange;
import no.dv8.functions.XFunction;
import no.dv8.xhtml.generation.elements.*;

import java.util.function.Predicate;

import static no.dv8.eks.semantic.Rels.*;

public class EksIndex implements Predicate<Exchange>, XFunction<Exchange, Exchange> {

    final EksResources resources;

    public EksIndex(EksResources resources) {
        this.resources = resources;
    }


    public article index() {
        return
          new article()
            .add(
              new section()
                .add(new h1("Misc"))
                .add(basicLinksAsList())
            ).add(
            new section()
              .add(new h1("Queries"))
              .add( resources.queriesAsList())
          ).add(
            new section()
              .add(new h1("Creators"))
              .add(new EksForms(resources).creatorForms())
          );
    }

    public ul basicLinksAsList() {
        return new ul()
          .add(new li().add(new a("index").rel(index).href(resources.urlCreator.root())))
          .add(new li().add(new a("self").rel(self).href(resources.urlCreator.root())))
          .add(new li().add(new a("profile").rel(profile).href("http://alps.io/spec/alps-xhtml-profiles/")));
    }

    @Override
    public boolean test(Exchange exchange) {
        return false;
    }

    @Override
    public Exchange apply(Exchange exchange) throws Exception {
        return null;
    }
}
