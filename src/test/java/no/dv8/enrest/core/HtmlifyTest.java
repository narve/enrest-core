package no.dv8.enrest.core;

import no.dv8.eks.model.Article;
import no.dv8.eks.model.Comment;
import no.dv8.eks.rest.BasicResource;
import no.dv8.eks.rest.EksLinker;
import no.dv8.eks.rest.EksResources;
import no.dv8.eks.semantic.Rels;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.writers.XHTMLWriter;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.support.Element;
import org.junit.Test;

import static java.util.Arrays.asList;
import static no.dv8.utils.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HtmlifyTest {

    EksResources resource() {
        EksResources resources = new EksResources("");
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

        EksResources resource = resource();

        assertThat(resource.locateByClz(Comment.class), isPresent());

        Element<?> element = new XHTMLWriter().toElement(art, new EksLinker(resource).apply(new Exchange(null, null)).getLinks());

        Element<?> links = element.getChildren().get(element.getChildren().size() - 1);
        assertThat(links.getChildren().size(), equalTo(3));

        a link = (a) links.getChildren().get(links.getChildren().size() - 1);
        assertThat(link.href().toString(), containsString("comment/123"));

        fail(element.toString());
    }

}
