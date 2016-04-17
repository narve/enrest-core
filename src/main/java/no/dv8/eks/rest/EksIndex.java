package no.dv8.eks.rest;

import no.dv8.xhtml.generation.elements.*;

import static no.dv8.eks.semantic.Rels.*;

public class EksIndex {

    final String basePath;

    public EksIndex(String basePath) {
        this.basePath = basePath;
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
              .add(new EksQueries(basePath).queriesAsList())
          ).add(
            new section()
              .add(new h1("Creators"))
              .add(new EksForms(basePath).creatorForms())
          );
    }

    public ul basicLinksAsList() {
        return new ul()
          .add(new li().add(new a("index").rel(index).href(basePath)))
          .add(new li().add(new a("self").rel(self).href(basePath)))
          .add(new li().add(new a("profile").rel(profile).href("http://alps.io/spec/alps-xhtml-profiles/")));
    }

}
