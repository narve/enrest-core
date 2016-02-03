package no.dv8.rest3;

import java.util.ArrayList;
import java.util.List;

public interface Linker {

    default <T> List<EnrestResource> linksFor(T t ) {
        return new ArrayList<>();
    }
}
