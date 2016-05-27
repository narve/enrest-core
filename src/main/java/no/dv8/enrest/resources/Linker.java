package no.dv8.enrest.resources;

import no.dv8.enrest.semantic.Rels;
import no.dv8.xhtml.generation.elements.a;

import java.util.List;

import static java.util.Arrays.asList;

@FunctionalInterface
public interface Linker<T> {
    List<a> links(T t);

    static <T> Linker<T> defaultLinker() {
        return t -> asList(
          new a("view " + t.toString()).href(t).rel(Rels.self),
          new a("edit " + t.toString()).href(t).rel(Rels.edit)
        );
    }
}
