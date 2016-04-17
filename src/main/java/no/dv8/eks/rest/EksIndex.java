package no.dv8.eks.rest;

import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.enrest.mutation.Locator;
import no.dv8.enrest.mutation.Mutator;
import no.dv8.enrest.mutation.Resource;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.reflect.Props;
import no.dv8.reflect.SimpleInput;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.dv8.eks.semantic.Rels.*;

public class EksIndex {

    final String basePath;

    public EksIndex(String basePath) {
        this.basePath = basePath;
    }

    public static List<Resource> resources() {
        return asList(
          new UserResource(),
          new QuestionResource(),
          basicResource(Article.class),
          basicResource(Comment.class)
        );
    }


    public static <T> Resource<T> basicResource(Class<T> clz ) {
        return new Resource<T>() {
            @Override
            public Class<T> clz() {
                return clz;
            }

            @Override
            public Locator<T> locator() {
                return s -> CRUD.create(clz).getById(s);
            }

            @Override
            public Mutator<T> creator() {
                return new Mutator<T>() {

                    @Override
                    public List<Element<?>> inputs(T t) {
                        return new Props().all(clz())
                          .stream()
                          .map( pd -> new SimpleInput<T>().apply(pd, t))
                          .collect(toList());
                    }

                    @Override
                    public T create(T t) {
                        return CRUD.create(clz).insert(t);
                    }

                    @Override
                    public T setProps(T target, HttpServletRequest req) {
                        return asList(req)
                          .stream()
                          .map(HttpServletRequest::getParameterMap)
                          .map(new Props()::single)
                          .map(m -> new Props().setProps(target, m))
                          .findFirst().get();
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
                return asList( new SimpleQuery<T>(clz.getSimpleName()+"Collection", s -> CRUD.create(clz).all() ));
            }
        };
    }

    public article index() {
        return
          new article()
            .add(
              new section()
                .add(new h1("Misc"))
                .add(basicLinksAsList())
            ).add(
            new section()
              .add(new h1("Queries"))
              .add(new EksQueries(basePath).queriesAsList())
          ).add(
            new section()
              .add(new h1("Creators"))
              .add(new EksForms(basePath).creatorForms())
          );
    }

    public ul basicLinksAsList() {
        return new ul()
          .add(new li().add(new a("index").rel(index).href(basePath)))
          .add(new li().add(new a("self").rel(self).href(basePath)))
          .add(new li().add(new a("profile").rel(profile).href("http://alps.io/spec/alps-xhtml-profiles/")));
    }

}
