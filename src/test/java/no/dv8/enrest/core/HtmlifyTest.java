package no.dv8.enrest.core;

import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.resources.BasicResource;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.handlers.LinkHandler;
import no.dv8.enrest.semantic.Rels;
import no.dv8.enrest.writers.XHTMLWriter;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.support.Element;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.utils.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HtmlifyTest {

    ResourceRegistry resource() {
        ResourceRegistry resources = new ResourceRegistry("/");
        BasicResource<Article> artResource = BasicResource.create(resources, Article.class);
        BasicResource<Comment> commentResource = BasicResource.create(resources, Comment.class);

        resources.resources().add(artResource);
        resources.resources().add(commentResource);

        Comment theComment = new Comment();
        theComment.setId(123L);
        artResource.linker = article -> asList(
          new a(article.toString()).href(article).rel(Rels.self),
          new a().href(article).rel(Rels.edit),
          new a().href(theComment).rel("theComment")
        );

        return resources;
    }

    @Test
    public void testArticle() {
        Article art = new Article();
        art.setId(456L);

        ResourceRegistry resource = resource();

        assertThat(resource.locateByClz(Comment.class), isPresent());

        List<a> links = new LinkHandler(resource)
          .apply(new Exchange(null, null).withOutEntity(new Article()))
          .getLinks();

        assertThat(links.size(), equalTo(3));

        a link = links.get(links.size() - 1);
        assertThat(link.href().toString(), containsString("comment/123"));

    }

}
