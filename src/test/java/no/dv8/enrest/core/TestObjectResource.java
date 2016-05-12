package no.dv8.enrest.core;

import no.dv8.enrest.resources.Locator;
import no.dv8.enrest.resources.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestObjectResource implements Resource<TestObject> {

    List<TestObject> objects = new ArrayList<>();

    public TestObjectResource() {
        objects.add(new TestObject("123", "val"));
        objects.add(new TestObject("12345", "val"));
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

