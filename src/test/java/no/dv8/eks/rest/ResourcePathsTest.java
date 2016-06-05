package no.dv8.eks.rest;

import no.dv8.enrest.ResourcePaths;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ResourcePathsTest {
    //    final String basePath = "/eks/api/";
    final String basePath;

    public ResourcePathsTest(String basePath) {
        this.basePath = basePath;
    }

    @Parameters
    public static Collection<Object[]> paths() {
        return asList(new Object[][]{
          {"/"},
          {"/api/"},
          {"/api/1.0/asdfasdf/"},
          {"/eks/api/"},
        });
    }

    @Test
    public void full() throws Exception {
        ResourcePaths rp = new ResourcePaths(basePath);
        assertThat(rp.full(""), equalTo(basePath));
        assertThat(rp.full("/"), equalTo(basePath));
        assertThat(rp.full("asdf"), equalTo(basePath + "asdf"));
        assertThat(rp.full("/asdf"), equalTo(basePath + "asdf"));
    }

    @Test
    public void pure() throws Exception {
        ResourcePaths rp = new ResourcePaths(basePath);

        assertThat(rp.pure(basePath + "asdf"), equalTo("asdf"));
        assertThat(rp.pure(basePath + "/asdf"), equalTo("asdf"));
        assertThat(rp.pure(basePath + ""), equalTo(""));
        assertThat(rp.pure(basePath + "/"), equalTo(""));
    }

    @Test
    public void type() throws Exception {
        ResourcePaths rp = new ResourcePaths(basePath);
        String url = rp.viewItem("thetype", "456");
        assertThat(rp.id( url), equalTo( "456" ) );
        assertThat(rp.type( url), equalTo( "thetype" ) );
    }

    @Test
    public void id() throws Exception {
        ResourcePaths rp = new ResourcePaths(basePath);
        assertThat(rp.id( basePath + "view-resource/thetype/456" ), equalTo( "456" ) );
    }

    @Test
    public void queryResult() throws Exception {
        ResourcePaths rp = new ResourcePaths(basePath);
        String url = basePath + "query-result/ArticleCollection?search=asdf";
        assertFalse(rp.isItem( url ));
        assertTrue( rp.isQueryResult(url));
    }

}