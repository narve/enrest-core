package no.dv8.rest3;

import no.dv8.rest2.framework.Link;

import java.util.ArrayList;
import java.util.List;

public interface Linker {

    default <T> List<Link> linksFrom(EnrestResource r, T t ) {
        return new ArrayList<>();
    }
}
