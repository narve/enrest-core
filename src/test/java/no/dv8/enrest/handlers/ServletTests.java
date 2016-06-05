package no.dv8.enrest.handlers;

import no.dv8.enrest.EnrestServlet;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.core.TestObject;
import no.dv8.enrest.core.TestObjectResource;
import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.elements.form;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import static java.lang.String.valueOf;
import static junit.framework.TestCase.assertTrue;
import static no.dv8.utils.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ServletTests {

    public static final String BASE_PATH = "/unit-test/";

    Resource<TestObject> resource;
    TestObject test1;
    EnrestServlet servlet;
    ResourceRegistry resources;

    @Before
    public void beforeTest() throws ServletException {
        resources = new ResourceRegistry(BASE_PATH);
        resource = new TestObjectResource(resources);
        resources.resources().add(resource);
        test1 = new TestObject();
        test1.setStringValue("stringval1");
        resource.creator().create(test1);
        servlet = new EnrestServlet(resources);
        servlet.init(null);
    }

    @Test
    public void testDeleteOnResource() {
        // Given
        assertThat( resource.locator().apply(test1.getId()), isPresent());

        // When
        resource.deleter().accept(test1.getId());

        // Then
        assertThat(resource.locator().apply(test1.getId()), not(isPresent()));
    }


    @Test
    public void testHTTPGET() throws ServletException {
        // Given:
        assertThat(resource.locator().apply(test1.getId()), isPresent());
        assertThat(resources.locateByClz(TestObject.class), isPresent());
        assertThat(resources.getByName(TestObject.class.getSimpleName()), not(is(nullValue())));

        Mock<HttpServletRequest> req = new Mock<>(HttpServletRequest.class)
          .throwIfUnset();
        initReq(req, resources.getPaths().viewItem( TestObject.class.getSimpleName(), test1.getId() ));
        req.set("getMethod", "GET");

        Mock<HttpServletResponse> res = new Mock<>(HttpServletResponse.class);
        StringWriter stringWriter = initRes(res);



        // When:
        servlet.service(req.instance(), res.instance());

        assertTrue( "not item: " + req.instance().getRequestURL(), resources.getPaths().isItem( req.instance().getRequestURL().toString() ) );

        assertThat(stringWriter.toString(), containsString("stringval1"));
    }



    @Test
    public void testHTTPDeleteForm() throws ServletException {
        // Given:
        assertThat(resource.locator().apply(test1.getId()), isPresent());
        assertThat(resources.locateByClz(TestObject.class), isPresent());
        assertThat(resources.getByName(TestObject.class.getSimpleName()), not(is(nullValue())));

        Mock<HttpServletRequest> req = new Mock<>(HttpServletRequest.class)
          .throwIfUnset();
        initReq(req, resources.getPaths().deleteForm( TestObject.class.getSimpleName(), test1.getId() ));
        req.set("getMethod", "GET");

        Mock<HttpServletResponse> res = new Mock<>(HttpServletResponse.class);
        StringWriter stringWriter = initRes(res);

        // When:
        servlet.service(req.instance(), res.instance());

        assertThat(stringWriter.toString(), containsString(test1.getId()));
        assertThat(stringWriter.toString(), containsString("form"));
    }



    @Test
    public void testHTTPDeleteByForm() throws ServletException {
        // Given:
        String pathToForm = resources.getPaths().deleteForm(TestObject.class.getSimpleName(), test1.getId());
        form f = new DeleteFormHandler(resources).apply(new MockExchange().withFullPath( pathToForm )).getOutEntity();

        Mock<HttpServletRequest> req = new Mock<>(HttpServletRequest.class)
          .throwIfUnset();
        String action = valueOf(f.get("action"));
        initReq(req, action);
        req.set("getMethod", "POST");

        Mock<HttpServletResponse> res = new Mock<>(HttpServletResponse.class);
        StringWriter stringWriter = initRes(res);


        // When:
        servlet.service(req.instance(), res.instance());

        // Then
        assertThat(resource.locator().apply(test1.getId()), not(isPresent()));
        try {
            Mock<HttpServletRequest> getReq = new Mock<>(HttpServletRequest.class)
              .throwIfUnset();
            initReq( getReq, resources.getPaths().viewItem( TestObject.class.getSimpleName(), test1.getId() ) );
            getReq.set("getMethod", "GET");

            Mock<HttpServletResponse> getRes = new Mock<>(HttpServletResponse.class);
            initRes(getRes);
            servlet.service(getReq.instance(), getRes.instance());
            fail("Should've excepted");
        } catch( RuntimeException e ) {
            // check exception maybe?
            //assertThat( getRes.instance().getStatus(), equalTo( 404 ) );
        }
    }


    @Test
    public void testHTTPDELETE() throws ServletException {
        // Given:

        Mock<HttpServletRequest> getReq = new Mock<>(HttpServletRequest.class)
          .throwIfUnset();
        initReq( getReq, resources.getPaths().viewItem( TestObject.class.getSimpleName(), test1.getId() ) );
        getReq.set("getMethod", "GET");

        Mock<HttpServletResponse> getRes = new Mock<>(HttpServletResponse.class);
        initRes(getRes);

        assertThat(resource.locator().apply(test1.getId()), isPresent());
        servlet.service(getReq.instance(), getRes.instance());


        // When:
        Mock<HttpServletRequest> deleteReq = new Mock<>(HttpServletRequest.class)
          .throwIfUnset();
        initReq( deleteReq, resources.getPaths().viewItem( TestObject.class.getSimpleName(), test1.getId() ) );
        deleteReq.set("getMethod", "DELETE");

        Mock<HttpServletResponse> deleteRes = new Mock<>(HttpServletResponse.class);
        initRes(deleteRes);

        servlet.service(deleteReq.instance(), deleteRes.instance());

        // Then
        assertThat(resource.locator().apply(test1.getId()), not(isPresent()));
        try {
            servlet.service(getReq.instance(), getRes.instance());
            fail("Should've excepted");
        } catch( RuntimeException e ) {
            // check exception maybe?
            //assertThat( getRes.instance().getStatus(), equalTo( 404 ) );
        }
    }

    private void initReq(Mock<HttpServletRequest> req, String url) {
        req.set("getServletPath", BASE_PATH);
        req.set("getPathInfo", url.substring(BASE_PATH.length()));
        req.set("getRequestURL", new StringBuffer(url));
        req.set("getHeader", "application/xhtml"); // Accept !
        req.set( "getParameterMap", new HashMap<String, String[]>() );
    }


    private StringWriter initRes(Mock<HttpServletResponse> res) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        res.set("getWriter", printWriter);
        return stringWriter;
    }


}