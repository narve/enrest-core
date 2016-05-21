package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.resources.QuestionResource;
import no.dv8.eks.resources.UserResource;
import no.dv8.eks.semantic.Rels;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.queries.Parameter;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.utils.Chainer;
import no.dv8.utils.Forker;
import no.dv8.xhtml.generation.elements.a;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksServlet.ServletBase;

@javax.servlet.annotation.WebServlet(urlPatterns = {ServletBase + "/*"})
@Slf4j
public class EksServlet extends HttpServlet {

    static final String ServletBase = "/eks";

    private ServletConfig config;
    private Chainer handler;

    private EksResources createResources(String path) {
        EksResources resources = new EksResources(path);

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
            public Collection<?> query(HttpServletRequest req) {
                return CRUD.create(Comment.class).getEM()
                  .createQuery("SELECT x FROM Comment x WHERE x.article.id = :articleId")
                  .setParameter("articleId", Long.parseLong(req.getParameter("article.id")))
                  .getResultList();
            }
        };
        commentResource.queries.add(commentsForArticle);
        commentResource.linker = comment -> asList(
          new a("view " + comment.toString()).href(comment).rel(Rels.self),
          new a("edit " + comment.toString()).href(comment).rel(Rels.edit),
          new a(comment.getArticle()).href(comment.getArticle()).rel("article")
        );

        String qhref = resources.urlCreator.queryResult("comments") + "?article.id=%s";
        artResource.linker = article -> asList(
          new a("view " + article.toString()).href(article).rel(Rels.self),
          new a("edit " + article.toString()).href(article).rel(Rels.edit),
          new a("comments for " + article).href(format(qhref, article.getId())).rel("comments")
        );
        return resources;
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) {
        handler.apply(new Exchange(req, res));
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }

    UnaryOperator<Exchange> reqLogger() {
        return x -> {
            log.info("Start of: {} {}", x.req.getMethod(), x.getFullPath());
            return x;
        };
    }

    UnaryOperator<Exchange> finisher() {
        return x -> {
            log.info("Done with {} {}", x.req.getMethod(), x.getFullPath());
            x.finish();
            return x;
        };
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        final String apiPath = "";
        this.config = config;
        handler = new Chainer<Exchange>()
          .add(reqLogger())
          .add(new Forker<Exchange, Exchange>().add("api", startsWith(apiPath), new EksApi(createResources(ServletBase + apiPath+"/"))))
          .add(finisher());
    }

    private Predicate<Exchange> startsWith(String s) {
        return x -> ( x.req.getPathInfo() == null ? "" : x.req.getPathInfo()).startsWith(s);
    }

}
