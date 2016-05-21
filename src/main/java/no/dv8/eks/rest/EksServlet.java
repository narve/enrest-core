package no.dv8.eks.rest;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.eks.semantic.Rels;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.model.Parameter;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.xhtml.generation.elements.a;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksServlet.ServletBase;
import static no.dv8.functions.ServletFunctions.exMeansFalse;
import static no.dv8.functions.ServletFunctions.startsWith;
import static no.dv8.functions.XUnaryOperator.hidex;

@javax.servlet.annotation.WebServlet(urlPatterns = {ServletBase + "/*"})
@Slf4j
public class EksServlet extends HttpServlet {

    public static final String ServletBase = "/eks";
    UnaryOperator<Exchange> handler;
    List<Pair<Predicate<HttpServletRequest>, UnaryOperator<Exchange>>> consumers;
    private ServletConfig config;

    private EksResources createResources() {
        EksResources resources = new EksResources(ServletBase + "/api/");

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

    UnaryOperator<Exchange> main() {
        return x -> {
            Optional<Pair<Predicate<HttpServletRequest>, UnaryOperator<Exchange>>> handler =
              consumers
                .stream()
                .filter(p -> exMeansFalse(p.getKey()).test(x.req))
                .findFirst();
            if (handler.isPresent()) {
                return handler.get().getValue().apply(x);
            } else {
                throw new NullPointerException("No handler for " + x.req.getPathInfo());
            }
        };
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;

        EksResources resources = createResources();

        consumers = asList(
//          new Pair<>(
//            r -> r.getPathInfo() == null,
//            hidex("Redirect", x -> {
//                x.res.sendRedirect(x.req.getRequestURI() + "/");
//                return x;
//            })
//          ),
          new Pair<>(
            startsWith("/api"),
            hidex("API", new EksApi(resources).api())
          )
        );

        handler = asList(
          reqLogger(), main(), finisher()
        ).stream()
          .reduce(UnaryOperator.identity(), (a, b) -> s -> b.apply(a.apply(s)));

    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

}
