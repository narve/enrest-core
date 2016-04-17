package no.dv8.enrest.mutation;

import no.dv8.enrest.queries.QueryResource;

import java.util.List;

public interface Resource<T> {

    Class<T> clz();

    Mutator<T> creator();

    Locator<T> locator();

    Mutator<T> updater();

    String getName();

    List<QueryResource> queries();

}
