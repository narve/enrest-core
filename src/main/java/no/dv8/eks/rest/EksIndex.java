package no.dv8.eks.rest;

import no.dv8.xhtml.generation.elements.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.eks.semantic.Rels.*;

@Stateless
public class EksIndex {

    @Inject
    EksQueries eksQueries = new EksQueries();

    @Inject
    EksForms eksForms = new EksForms();

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
              .add(eksQueries.queriesAsList())
          ).add(
            new section()
            .add( new h1( "Creators" ) )
            .add(eksForms.creatorForms())
          );
    }

    public ul basicLinksAsList() {
        return new ul()
          .add(new li().add(new a("index").rel(index).href("/eks/")))
          .add(new li().add(new a("self").rel(self).href("/eks/")))
          .add(new li().add(new a("profile").rel(profile).href("http://alps.io/spec/alps-xhtml-profiles/")));
    }

}
