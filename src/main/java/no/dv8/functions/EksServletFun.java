package no.dv8.functions;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.CRUD;
import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.resources.QuestionResource;
import no.dv8.eks.resources.UserResource;
import no.dv8.eks.resources.BasicResource;
import no.dv8.eks.rest.EksHTML;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.semantic.Rels;
import no.dv8.enrest.queries.Parameter;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.utils.Pair;
import no.dv8.utils.Props;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.dv8.functions.ServletFunctions.consumer;
import static no.dv8.functions.ServletFunctions.startsWith;
import static no.dv8.functions.XBiConsumer.hidex;

@Slf4j
public class EksServletFun {

    Function<HttpServletRequest, Article> reqProcessor = hr -> new Article();
    Function<Article, Long> al = Article::getId;
    Function<Long, String> ls = String::valueOf;
    Function<Article, String> f1 = al.andThen(ls);
    Function<Article, String> f2 = ls.compose(al);



    List<Pair<Predicate<HttpServletRequest>, BiConsumer<HttpServletRequest, HttpServletResponse>>> consumers() {
        ResourceRegistry resources = new ResourceRegistry("justsomestuff");

        BasicResource<Article> artResource = BasicResource.create(resources, Article.class);
//        artResource.linker = t -> asList(
//        );

        resources.resources().add(new UserResource());
        resources.resources().add(new QuestionResource());
        resources.resources().add(artResource);
        BasicResource<Comment> commentResource = BasicResource.create(resources, Comment.class);
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

        String qhref = resources.getPaths().queryResult("comments") + "?article.id=%s";
//        String qhref = "http://localhost:8080/eks/api/query-result/comments?article.id=%s";
        artResource.linker = article -> asList(
          new a("view " + article.toString()).href(article).rel(Rels.self),
          new a("edit " + article.toString()).href(article).rel(Rels.edit),
          new a("comments for " + article).href(format(qhref, article.getId())).rel("comments")
        );

        return asList(
          new Pair<>(
            r -> r.getPathInfo() == null,
            hidex("Redirect", (req, res) -> res.sendRedirect(req.getRequestURI() + "/"))
          ),
//          new Pair<>(
//            startsWith("/_files"),
//            hidex(new FileHandler("/home/narve/", "_files"))
//          ),
          new Pair<>(
            startsWith("/_test1"),
            hidex(new EksServletFun().test1())
          ),
          new Pair<>(
            startsWith("/Comment"),
            hidex(new EksServletFun().test3(Comment.class))
          ),
          new Pair<>(
            startsWith("/_test2"),
            hidex(new EksServletFun().test2())
          ),
          new Pair<>(
            startsWith("/_user"),
            hidex(new UserResource().testBIC())
          ),
//          new Pair<>(
//            startsWith("/api"),
//            hidex(new EksApi(resources).api())
//          ),
          new Pair<>(
            startsWith("/"),
//            hidex((req, res) -> res.getWriter().write(CoreMatchers.startsWith("asdf").toString()) )
            hidex(listBIC())
          )
        );
    }


    Function<HttpServletRequest, Article> testFunc() {
        return req -> new Article();
    }

    XBiConsumer<HttpServletRequest, HttpServletResponse> testBIC() {
        return consumer(testFunc());
    }

    XBiConsumer<HttpServletRequest, HttpServletResponse> listBIC() {
        return (req, res) -> res.getWriter().write(EksHTML.complete(listConsumers(), "Index").toHTML());
    }

    Element<?> listConsumers() {
        return new ul()
          .add(
            consumers()
              .stream()
              .map(p -> new li().add(p.getKey() + " => " + p.getValue()))
              .collect(toList())
          );
    }

    <T> Function<T, String> serializer() {
        return Object::toString;
    }

    XBiConsumer<HttpServletResponse, String> sender() {
        return (r, s) -> {
            r.getWriter().print(s);
        };
    }

    public XBiConsumer<HttpServletRequest, HttpServletResponse> test1() {
        return
          (in, out) -> sender().accept(
            out, reqToMap()
              .andThen(mapToObj(Article.class))
              .andThen(a -> {
                  log.info("Saving: {}", a);
                  return a;
              })
              .andThen(saver(Article.class))
              .andThen(serializer())
              .apply(in)
          );
    }

    public XBiConsumer<HttpServletRequest, HttpServletResponse> test2() {
        return
          consumer(
            reqToMap()
              .andThen(mapToObj(Article.class))
              .andThen(a -> {
                  log.info("Saving: {}", a);
                  return a;
              })
              .andThen(saver(Article.class))
          );
    }

    public <T> XBiConsumer<HttpServletRequest, HttpServletResponse> test3(Class<T> clz) {
        return
          consumer(
            reqToMap()
              .andThen(mapToObj(clz))
              .andThen(a -> {
                  log.info("Saving: {}", a);
                  return a;
              })
              .andThen(saver(clz))
          );
    }

    Function<HttpServletRequest, Map<String, String>> reqToMap() {
        return r -> new Props().single(r.getParameterMap());
    }

    <T> Function<Map<String, String>, T> mapToObj(Class<T> clz) {
        return m -> new Props().createAndSetProps(clz, m);
    }

    <T> Function<T, T> saver(Class<T> clz) {
        return a -> {
            CRUD<T> crud = new CRUD<>(clz);
            a = crud.insert(a);
            log.info("size: {}", crud.all().size());
            return a;
        };
    }


}
