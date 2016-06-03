package no.dv8.enrest.handlers;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.EnrestServlet;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.core.TestObject;
import no.dv8.enrest.core.TestObjectResource;
import no.dv8.enrest.resources.Resource;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static no.dv8.utils.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ItemHandlerTest {

    public static final String BASE_PATH = "/unit-test/";

    @Test
    public void testDeleteOnResource() {
        // Given:
        Resource<TestObject> resource = new TestObjectResource();
        TestObject test1 = new TestObject();
        test1.setStringValue("val1");
        resource.creator().create(test1);

        // When:
        Optional<TestObject> got = resource
                .locator()
                .apply(test1.getId());
        assertThat(got, isPresent());
        resource.deleter().deleteById(test1.getId());
        assertThat(resource.locator().apply(test1.getId()), not(isPresent()));
    }


    @Test
    public void testGETByHTTP() throws ServletException {
        // Given:
        Resource<TestObject> resource = new TestObjectResource();
        TestObject test1 = new TestObject();
        test1.setStringValue("stringval1");
        resource.creator().create(test1);

        EnrestServlet servlet = new EnrestServlet() {
            @Override
            public ResourceRegistry createResources() {
                ResourceRegistry resources = new ResourceRegistry(BASE_PATH);
                resources.resources().add(resource);
                return resources;
            }
        };

        ResourceRegistry resources = servlet.createResources();

        assertThat(resource.locator().apply(test1.getId()), isPresent());

        assertThat(resources.locateByClz(TestObject.class), isPresent());
        assertThat(resources.getByName(TestObject.class.getSimpleName()), not(is(nullValue())));


        // When:
        Mock<HttpServletRequest> req = new Mock<>(HttpServletRequest.class)
          .throwIfUnset();

        String url = resources.urlCreator.viewItem( TestObject.class.getSimpleName(), test1.getId() );

        req.set("getServletPath", BASE_PATH);
        req.set("getPathInfo", url.substring(BASE_PATH.length()));
        req.set("getRequestURL", new StringBuffer(url));
        req.set("getMethod", "GET");
        req.set("getHeader", "application/xhtml");
        req.set( "getParameterMap", new HashMap<String, String[]>() );

        Mock<HttpServletResponse> res = new Mock<>(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        res.set("getWriter", printWriter);

        servlet.init(null);
        servlet.service(req.instance(), res.instance());

        assertTrue( "not item: " + req.instance().getRequestURL(), resources.urlCreator.isItem( req.instance().getRequestURL().toString() ) );

        assertThat(stringWriter.toString(), containsString("stringval1"));
    }


    @Test
    public void testDeleteByHTTP() throws ServletException {
        // Given:
        Resource<TestObject> resource = new TestObjectResource();
        TestObject test1 = new TestObject();
        test1.setStringValue("stringval1");
        resource.creator().create(test1);

        EnrestServlet servlet = new EnrestServlet() {
            @Override
            public ResourceRegistry createResources() {
                ResourceRegistry resources = new ResourceRegistry(BASE_PATH);
                resources.resources().add(resource);
                return resources;
            }
        };

        ResourceRegistry resources = servlet.createResources();

        assertThat(resource.locator().apply(test1.getId()), isPresent());

        // When:
        Mock<HttpServletRequest> req = new Mock<>(HttpServletRequest.class)
          .throwIfUnset();

        String url = resources.urlCreator.viewItem( TestObject.class.getSimpleName(), test1.getId() );
        req.set("getServletPath", BASE_PATH);
        req.set("getPathInfo", url.substring(BASE_PATH.length()));
        req.set("getRequestURL", new StringBuffer(url));
        req.set("getMethod", "DELETE");
        req.set("getHeader", "application/xhtml");
        req.set( "getParameterMap", new HashMap<String, String[]>() );

        Mock<HttpServletResponse> res = new Mock<>(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        res.set("getWriter", printWriter);

        servlet.init(null);
        servlet.service(req.instance(), res.instance());

        assertThat(resource.locator().apply(test1.getId()), not(isPresent()));
    }


    @Slf4j
    static class Mock<T> {
        T t;
        InvocationHandler handler;
        Map<String, Object> vals = new HashMap<>();
        boolean throwIfUnset = false;

        public Mock(Class<T> clz) {
            handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String key = method.getName();
                    if (vals.containsKey(key)) {
                        return vals.get(key);
                    } else {
                        log.info("INVOKE {}.{} => N/A", clz.getSimpleName(), method.getName());
                        // maybe?
                        // return null;
                        if (throwIfUnset)
                            throw new IllegalStateException("INVOKED: " + method.getName());
                        else
                            return null;
                    }
                }
            };
            t = (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, handler);
        }

        public T instance() {
            return t;
        }


        public void set(String methodName, Object returnValue) {
            vals.put(methodName, returnValue);
        }

        public Mock<T> throwIfUnset() {
            throwIfUnset = true;
            return this;
        }
    }
}