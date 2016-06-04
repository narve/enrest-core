package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.resources.BasicResource;
import no.dv8.eks.resources.QuestionResource;
import no.dv8.eks.resources.UserResource;
import no.dv8.enrest.resources.Linker;
import no.dv8.enrest.semantic.Rels;
import no.dv8.enrest.EnrestServlet;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.queries.Parameter;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.xhtml.generation.elements.a;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksServlet.ServletBase;

@javax.servlet.annotation.WebServlet(urlPatterns = {ServletBase + "/*"})
@Slf4j
public class EksServlet extends EnrestServlet {

    static final String ServletBase = "/eks";

    public ResourceRegistry createResources() {
        ResourceRegistry resources = new ResourceRegistry(ServletBase + "/");

        BasicResource<Article> artResource = BasicResource.create(resources, Article.class);
        BasicResource<Comment> commentResource = BasicResource.create(resources, Comment.class);
        resources.resources().add(new UserResource());
        resources.resources().add(new QuestionResource());
        resources.resources().add(artResource);
        resources.resources().add(commentResource);

        QueryResource commentsForArticle = new QueryResource() {
            @Override
            public String getRel() {
                return "comments";
            }

            @Override
            public List<Parameter> params() {
                return asList(
                  new Parameter("article.id", Long.class.getSimpleName(), "number", null)
                );
            }

            @Override
            public Collection<?> query(Map<String, String[]> parameters) {
                return CRUD.create(Comment.class).getEM()
                  .createQuery("SELECT x FROM Comment x WHERE x.article.id = :articleId")
                  .setParameter("articleId", Long.parseLong(parameters.get("article.id")[0]))
                  .getResultList();
            }
        };
        commentResource.queries.add(commentsForArticle);
        commentResource.linker = comment -> asList(
          new a("view " + comment.toString()).href(comment).rel(Rels.self),
          new a("edit " + comment.toString()).href(comment).rel(Rels.edit),
          new a(comment.getArticle()).href(comment.getArticle()).rel("article")
        );

        String qhref = resources.getPaths().queryResult("comments") + "?article.id=%s";
        artResource.linker = article -> {
            List<a> links = new ArrayList<>();
            links.addAll( artResource.defaultLinker().links(article));
            links.addAll(
            asList(
              new a("comments for " + article).href(format(qhref, article.getId())).rel("comments")
            ));
            return links;
        };
        return resources;
    }

}
