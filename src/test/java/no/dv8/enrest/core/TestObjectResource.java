package no.dv8.enrest.core;

import no.dv8.enrest.props.PropsMapper;
import no.dv8.enrest.resources.Locator;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.reflect.Props;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class TestObjectResource implements Resource<TestObject> {

    List<TestObject> objects = new ArrayList<>();

    @Override
    public Mutator<TestObject> creator() {
        return new Mutator<TestObject>() {

            @Override
            public TestObject create(TestObject testObject) {
                testObject.setId(UUID.randomUUID().toString());
                objects.add(testObject);
                return testObject;
            }

            @Override
            public TestObject setProps(TestObject target, HttpServletRequest req) {
                return target;
            }

        };
    }

    @Override
    public Mutator<TestObject> updater() {
        return null;
    }

    public TestObjectResource() {
        objects.add(new TestObject("123", "val1231"));
        objects.add(new TestObject("12345", "val12345"));
    }

    @Override
    public Class<TestObject> clz() {
        return TestObject.class;
    }

    @Override
    public Locator<TestObject> locator() {
        return new Locator<TestObject>() {
            @Override
            public Optional<TestObject> getById(String id) {
                return objects.stream().filter(t -> id.equals(t.getId())).findFirst();
            }
        };
    }
}

