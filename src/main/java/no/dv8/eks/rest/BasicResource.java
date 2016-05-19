package no.dv8.eks.rest;

import no.dv8.eks.controllers.CRUD;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.enrest.resources.Linker;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class BasicResource<T> implements Resource<T> {
    private final Class<T> clz;
    private final EksResources owner;

    public Linker<T> linker = Linker.defaultLinker();
    public Function<String, Optional<T>> locator = s -> Optional.ofNullable(CRUD.create(clz()).getById(s));
    public Mutator<T> updater = crudMutator();
    public Mutator<T> creator = crudMutator();
    public List<QueryResource> queries;

    public static <T> BasicResource<T> create( EksResources owner, Class<T> clz ) {
        return new BasicResource<>(owner, clz);
    }

    public BasicResource(EksResources owner, Class<T> clz) {
        this.clz = clz;
        this.owner = owner;
        this.queries = asList(
          new SimpleQuery<T>(getName() + "Collection", s -> CRUD.create(clz()).all())
        );
    }

    @Override
    public Class<T> clz() {
        return clz;
    }

    @Override
    public Function<String, Optional<T>> locator() {
        return locator;
    }

    @Override
    public Linker<T> linker() {
        return linker;
    }

    @Override
    public Mutator<T> creator() {
        return creator;
    }

    @Override
    public Mutator<T> updater() {
        return updater;
    }

    @Override
    public String getName() {
        return clz.getSimpleName();
    }

    @Override
    public List<QueryResource> queries() {
        return queries;
    }

    public Mutator<T> crudMutator() {
        return new Mutator<T>() {

            @Override
            public T create(T t) {
                return CRUD.create(clz).insert(t);
            }

            @Override
            public T update(T t) {
                return CRUD.create(clz).update(t);
            }
        };
    }

    @Override
    public String toString() {
        return "BasicResource{" +
          "clz=" + clz +
          '}';
    }
}
