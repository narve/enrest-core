package no.dv8.enrest.resources;

import no.dv8.enrest.queries.QueryResource;

import java.util.List;

import static java.util.Collections.emptyList;

public interface Resource<T> {

    Class<T> clz();

    default String getName() {
        return clz().getSimpleName();
    }

    Mutator<T> creator();

    Mutator<T> updater();

    Locator<T> locator();

    List<QueryResource> queries();

    default Linker<T> linker() {
        return t -> emptyList();
    }

}
