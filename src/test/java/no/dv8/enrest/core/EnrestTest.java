package no.dv8.enrest.core;

import no.dv8.enrest.container.Enrest;
import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.support.Element;
import org.junit.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.dv8.utils.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class EnrestTest {

    public Resource<TestObject> resource() {
        return new TestObjectResource();
    }

    @Test
    public void testResourceLookup() {
        assertTrue(resource().locator().getById("123").isPresent());
        assertFalse(resource().locator().getById("1234").isPresent());
        assertThat(resource().locator().getById("12345").get().getId(), equalTo("12345"));
    }

    @Test
    public void testContainerLookup() {
        Enrest enrest = new Enrest().add(resource());
        assertThat(enrest.locateResource("TestObject"), notNullValue());
        assertThat(enrest.locateResource("TestObject").locator().getById("123"), isPresent());
        assertThat(enrest.locateResource("TestObject").locator().getById("1234"), not(isPresent()));
    }

    @Test
    public void testCreation() {
        TestObject t = new TestObject();
        t.setNested(new NestedTestObject());
        String val1 = "zxcv";
        Long val2 = 654987L;
        t.getNested().setNestedPrimitiveLong(val2);
        t.setStringValue("zxcv");
        List<Element<?>> inputs = resource().creator().inputs(t);
        assertThat( inputs.size(), equalTo(4));

        String reduced = inputs.stream().map(e -> e.toString()).reduce("", (a, b) -> a + b);

        assertThat( reduced, containsString( "input" ));
        assertThat( reduced, containsString( "value='" + val1 + "'" ));
        assertThat( reduced, containsString( "value='" + val2 + "'" ));

    }

}
