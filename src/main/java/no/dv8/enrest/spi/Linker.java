package no.dv8.enrest.spi;

import no.dv8.enrest.EnrestResource;
import no.dv8.enrest.model.Link;

import java.util.ArrayList;
import java.util.List;

public interface Linker {

    default <T> List<Link> linksFrom(EnrestResource r, T t ) {
        return new ArrayList<>();
    }
}
