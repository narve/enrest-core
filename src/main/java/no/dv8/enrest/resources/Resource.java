package no.dv8.enrest.resources;

import no.dv8.enrest.queries.QueryResource;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public interface Resource<T> {

    Class<T> clz();

    Locator<T> locator();

    default String getName() {
        return clz().getSimpleName();
    }

    default Mutator<T> creator() {
        return null;
    }

    default Mutator<T> updater() {
        return null;
    }

    default List<QueryResource> queries() {
        return new ArrayList<>();
    }

    default Linker<T> linker() {
        return t -> emptyList();
    }

}
