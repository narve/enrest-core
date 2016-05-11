package no.dv8.enrest.resources;

import no.dv8.xhtml.generation.support.Element;

import java.util.List;

@FunctionalInterface
public interface Linker<T> {
    List<Element<?>> links(T t);
}
