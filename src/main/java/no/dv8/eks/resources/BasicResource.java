package no.dv8.eks.resources;

import no.dv8.eks.controllers.CRUD;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.enrest.resources.Linker;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.enrest.semantic.Rels;
import no.dv8.xhtml.generation.elements.a;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class BasicResource<T> implements Resource<T> {
    public final Class<T> clz;
    public final ResourceRegistry owner;

    public Linker<T> linker = defaultLinker();
    public Function<String, Optional<T>> locator = s -> Optional.ofNullable(CRUD.create(clz()).getById(s));
    public Mutator<T> updater = crudMutator();
    public Mutator<T> creator = crudMutator();
    public Mutator<T> deleter = crudMutator();
    public List<QueryResource> queries;
    public BasicResource(ResourceRegistry owner, Class<T> clz) {
        this.clz = clz;
        this.owner = owner;
        this.queries = new ArrayList<>(asList(
          new SimpleQuery<T>(clz.getSimpleName() + "Collection", s -> CRUD.create(clz()).all())
        ));
    }

    public static <T> BasicResource<T> create(ResourceRegistry owner, Class<T> clz) {
        return new BasicResource<>(owner, clz);
    }

    public Linker<T> defaultLinker() {
        return t -> asList(
          new a("view " + t.toString()).href(t).rel(Rels.self),
          new a("edit " + t.toString()).href(t).rel(Rels.edit),
          new a("delete " + t.toString())
            .rel(Rels.delete_form)
            .href(owner.getPaths().deleteForm(t.getClass().getSimpleName(), owner.itemId(t))
            )
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
    public Mutator<T> deleter() {
        return deleter;
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

            @Override
            public void deleteById(String t) {
                CRUD.create(clz).deleteById(t);
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
