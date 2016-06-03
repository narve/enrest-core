package no.dv8.enrest.resources;

import no.dv8.enrest.queries.QueryResource;
import no.dv8.xhtml.generation.elements.a;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public interface Resource<T> {

    Class<T> clz();

    Function<String, Optional<T>> locator();

    default String getName() {
        return clz().getSimpleName();
    }

    default Mutator<T> creator() {
        return null;
    }

    default Mutator<T> updater() {
        return null;
    }

    default Mutator<T> deleter() {
        return null;
    }

    default List<QueryResource> queries() {
        return new ArrayList<>();
    }

    default Linker<T> linker() {
        return t -> emptyList();
//        return item -> asList(
//          new a( "Edit " + item.toString()).href( editUrlForItem(item)).rel( "edit")
//        )
//        );
    }

}
