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
        assertTrue(resource().locator().apply("123").isPresent());
        assertFalse(resource().locator().apply("1234").isPresent());
        assertThat(resource().locator().apply("12345").get().getId(), equalTo("12345"));
    }

    @Test
    public void testContainerLookup() {
        Enrest enrest = new Enrest().add(resource());
        assertThat(enrest.locateResource("TestObject"), notNullValue());
        assertThat(enrest.locateResource("TestObject").locator().apply("123"), isPresent());
        assertThat(enrest.locateResource("TestObject").locator().apply("1234"), not(isPresent()));
    }

}
