package no.dv8.eks.rest;

import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.semantic.Rels;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.enrest.resources.Linker;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.elements.a;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class BasicResource<T> implements Resource<T> {
    private final Class<T> clz;

    public Linker<T> linker = t -> asList(
      new a("view " + t.toString()).href(t).rel(Rels.self),
      new a("edit " + t.toString()).href(t).rel(Rels.edit)
    );

    public BasicResource(Class<T> clz) {
        this.clz = clz;
    }

    @Override
    public Class<T> clz() {
        return clz;
    }

    @Override
    public Function<String, Optional<T>> locator() {
        return s -> Optional.ofNullable(CRUD.create(clz).getById(s));
    }

    @Override
    public Linker<T> linker() {
        return linker;
    }

    @Override
    public Mutator<T> creator() {
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
    public Mutator<T> updater() {
        return creator();
    }

    @Override
    public String getName() {
        return clz.getSimpleName();
    }

    @Override
    public List<QueryResource> queries() {
        return asList(new SimpleQuery<T>(clz.getSimpleName() + "Collection", s -> CRUD.create(clz).all()));
    }
}
