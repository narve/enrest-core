package no.dv8.eks.rest;

import no.dv8.eks.model.Article;
import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.enrest.mutation.Mutator;
import no.dv8.enrest.mutation.Resource;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.reflect.Props;
import no.dv8.reflect.SimpleInput;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.dv8.eks.semantic.Rels.*;

@Stateless
public class EksIndex {

    public static List<Resource> resources() {
        return asList(
                new UserResource(),
                new QuestionResource(),
                articleResource()
        );
    }


    public static Resource<Article> articleResource() {
        return new Resource<Article>() {
            @Override
            public Class<Article> clz() {
                return Article.class;
            }

            @Override
            public Mutator<Article> creator() {
                return new Mutator<Article>() {

                    @Override
                    public List<Element<?>> inputs(Article article) {
                        return new Props().all(clz())
                                .stream()
                                .map(new SimpleInput()::apply)
                                .collect( toList() );
                    }

                    @Override
                    public Article create(Article article) {
                        return article;
                    }

                    @Override
                    public Article setProps(Article target, HttpServletRequest req) {
                        return asList(req)
                                .stream()
                                .map( HttpServletRequest::getParameterMap )
                                .map( new Props()::single )
                                .map( m -> new Props().setProps(article, m ) )
                                .findFirst().get();
                    }

                    @Override
                    public Article update(Article article) {
                        return null;
                    }
                };
            }

            @Override
            public Mutator<Article> updater() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public List<QueryResource> queries() {
                return null;
            }
        }
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
                                .add(new EksQueries().queriesAsList())
                ).add(
                        new section()
                                .add(new h1("Creators"))
                                .add(new EksForms().creatorForms())
                );
    }

    public ul basicLinksAsList() {
        return new ul()
                .add(new li().add(new a("index").rel(index).href("/eks/")))
                .add(new li().add(new a("self").rel(self).href("/eks/")))
                .add(new li().add(new a("profile").rel(profile).href("http://alps.io/spec/alps-xhtml-profiles/")));
    }

}
