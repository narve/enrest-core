package no.dv8.enrest.writers;

import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JSONWriterTest {
    @Test
    public void transformToMap() throws Exception {
        // Given:
        ul l = new ul()
          .add(new li().add(new a("asdf").href( "href1")))
          .add(new li().add(new a("qwer").rel( "rel")));

        // When:
        Object o = new JSONWriter().transformToMap(l);

        // Then:
//        fail( o.toString() );
        assertThat(o, Matchers.is(instanceOf(Collection.class)));

    }

}