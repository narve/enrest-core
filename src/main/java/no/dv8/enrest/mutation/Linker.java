package no.dv8.enrest.mutation;

import no.dv8.enrest.model.Link;
import no.dv8.xhtml.generation.elements.link;
import no.dv8.xhtml.generation.support.Element;

import java.util.List;

import static java.util.Collections.emptyList;

@FunctionalInterface
public interface Linker<T> {
    List<Element<?>> links(T t);
}
