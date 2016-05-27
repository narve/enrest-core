package no.dv8.eks.rest;

import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

public class EksHTML {

    public static html complete(Element<?> elem, String title) {
        if (title == null || title.trim().isEmpty())
            title = "Eks";
        return new html()
          .add(
            new head()
              .set("profile", "http://alps.io/spec/alps-xhtml-profiles/")
              .add(new title(title))

              .add(new script().type("text/javascript").set("async", "async").src("https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=AM_CHTML"))

              .add(new link().type("text/css").href("/css/developer.css").rel("alternate stylesheet").set("title", "developer"))
              .add(new link().type("text/css").href("/css/user.css").rel("alternate stylesheet").set("title", "user"))

              .add(new link().type("text/css").href("/css/common.css").rel("stylesheet"))
              .add(new meta().charset("UTF-8"))
              .add(new meta().set("http-equiv", "Content-Type").set("content", "text/html").charset("utf-8"))
          ).add(
            new body()
              .add(new h1(title))
              .add(elem)
          );
    }

    public static a relToA(Object rel, String href) {
        return new a(rel).rel(rel).href(href);
    }


}
