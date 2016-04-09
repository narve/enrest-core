package no.dv8.eks.rest;

import no.dv8.eks.semantic.Rels;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

public class EksHTML {

    public static html complete(Element<?> elem, String title) {
        if( title == null || title.trim().isEmpty() )
            title = "Eks";
        return new html()
          .add(
            new head()
              .set("profile", "http://alps.io/spec/alps-xhtml-profiles/")
              .add( new title(title))
               .add( new link().rel( "stylesheet").type( "text/stylesheet" ).href( "/forms.css"))
          ).add(
            new body()
              .add(new h1(title))
              .add(elem)
          );
    }

    public static a relToA(Object rel, String prefix) {
        return new a(rel.toString()).rel(rel).href(prefix+rel.toString());
    }

    public static a relToA(Rels rel) {
        return relToA(rel, "");
    }
}
