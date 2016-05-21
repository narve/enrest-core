package no.dv8.enrest.core;

import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;

import java.util.*;
import java.util.function.Function;

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
            public TestObject setProps(TestObject target, Map<String, String> req) {
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
    public Function<String, Optional<TestObject>> locator() {
        return id -> objects.stream().filter(t -> id.equals(t.getId())).findFirst();
    }
}

