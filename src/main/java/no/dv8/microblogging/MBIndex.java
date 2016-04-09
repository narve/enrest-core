package no.dv8.microblogging;

import no.dv8.eks.semantic.Ids;
import no.dv8.eks.semantic.Rels;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import static no.dv8.eks.semantic.Rels.*;

public class MBIndex {

    public static a relToA(Rels rel, String prefix) {
        return new a(rel.toString()).rel(rel).href(prefix+rel.toString());
    }
    public static a relToA(Rels rel) {
        return relToA(rel, "");
    }

    public static html complete(Element<?> elem) {
        return new html()
          .add(
            new head()
              .set("profile", "http://alps.io/spec/alps-xhtml-profiles/")
            .add( new title("Microblogging"))
          ).add(
            new body()
              .add(new h1("Microblogging!"))
              .add(elem)
          );
    }

    div index() {
        return
          new div()
            .add(new a("index").rel(index).href("/mb/"))
            .add(new a("profile").rel("profile").href("http://alps.io/spec/alps-xhtml-profiles/"))
            .add(queries())
            .add(formLinks());
    }

    div queries() {
        return new div()
          .id(Ids.queries)
          .add(new h1(Ids.queries.toString()))
//          .add(relToA(messages_all))
//          .add(relToA(users_all))
//          .add(relToA(messages_search, "forms/"))
//          .add(relToA(users_search, "forms/"))
          ;
    }

    div formLinks() {
        return new div()
          .id("form-links")
          .add(new h1("form-links"))
//          .add(relToA(Rels.user_add, "forms/"))
//          .add(relToA(Rels.message_post, "forms/"))
          ;
    }

}
