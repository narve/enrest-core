package no.dv8.enrest.handlers;

import no.dv8.enrest.EnrestServlet;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.core.TestObject;
import no.dv8.enrest.core.TestObjectResource;
import no.dv8.enrest.resources.Resource;
import no.dv8.enrest.semantic.Rels;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.form;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static no.dv8.utils.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class DeleteByFormHandlerTest {

    public static final String BASE_PATH = "/unit-test/";

    UnaryOperator<Exchange> handler;
    TestObject test1;
    ResourceRegistry resources;
    Resource<TestObject> resource;

    @Before
    public void beforeTest() throws ServletException {
        resources = new ResourceRegistry(BASE_PATH);
        resource = new TestObjectResource(resources);

        test1 = new TestObject();
        test1.setStringValue("stringval1");
        resource.creator().create(test1);

        resources.resources().add(resource);
        handler = EnrestServlet.defaultChain(resources);
    }

    @Test
    public void canGetDeleteForm() {
        Exchange x = new MockExchange()
          .withFullPath(resources.getPaths().deleteForm(TestObject.class.getSimpleName(), test1.getId()))
          .withPathInfo(resources.getPaths().deleteForm(TestObject.class.getSimpleName(), test1.getId()))
          .withMethod("GET");
        MockExchange apply = (MockExchange) handler.apply(x);

        form f = apply.getOutEntity();
        assertThat(f.get("action"), equalTo(resources.getPaths().deleteFormResult(TestObject.class.getSimpleName(), test1.getId())));
    }

    @Test
    public void hasLinkToDeleteForm() {
        Exchange x = new MockExchange()
          .withFullPath(resources.getPaths().viewItem(TestObject.class.getSimpleName(), test1.getId()))
          .withPathInfo(resources.getPaths().viewItem(TestObject.class.getSimpleName(), test1.getId()))
          .withMethod("GET");
        MockExchange apply = (MockExchange) handler.apply(x);

        Optional<a> rel = apply.getLinks().stream()
          .filter(a -> a.get("rel").equals(Rels.delete_form))
          .findFirst();
        assertThat( "Links: " + apply.getLinks(), rel, isPresent() );

        String expPath = resources.getPaths().deleteForm(TestObject.class.getSimpleName(), test1.getId());
        assertThat( rel.get().href(), equalTo(expPath));
    }

    @Test
    public void canDeleteUsingForm() {

        assertThat(resource.locator().apply(test1.getId()), isPresent());

        Exchange x = new MockExchange()
          .withFullPath(resources.getPaths().deleteFormResult(TestObject.class.getSimpleName(), test1.getId()))
          .withPathInfo(resources.getPaths().deleteFormResult(TestObject.class.getSimpleName(), test1.getId()))
          .withMethod("GET");
        MockExchange apply = (MockExchange) handler.apply(x);

        form f = apply.getOutEntity();
        assertThat(f, nullValue());

        assertThat(resource.locator().apply(test1.getId()), not(isPresent()));
        assertThat(apply.getStatus(), equalTo(HttpURLConnection.HTTP_NO_CONTENT));

    }

}