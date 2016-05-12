package no.dv8.enrest.core;

import no.dv8.enrest.container.Enrest;
import no.dv8.enrest.resources.Resource;
import org.junit.Test;

import static no.dv8.utils.OptionalMatcher.isPresent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class EnrestTest {

    public Resource<TestObject> resource() {
        return new TestObjectResource();
    }

    @Test
    public void testResourceLocator() {
        assertTrue(resource().locator().getById("123").isPresent());
        assertFalse(resource().locator().getById("1234").isPresent());
        assertThat(resource().locator().getById("12345").get().getId(), equalTo("12345"));
    }

    @Test
    public void testContainerLookup() {
        Enrest enrest = new Enrest();
        enrest.add(resource());
        assertThat(enrest.locateResource("TestObject"), notNullValue());
        assertThat(enrest.locateResource("TestObject").locator().getById("123"), isPresent());
        assertThat(enrest.locateResource("TestObject").locator().getById("1234"), not(isPresent()));
    }

}
