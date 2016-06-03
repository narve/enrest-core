package no.dv8.enrest.resources;

import no.dv8.utils.PropsMapper;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.support.Element;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public interface Mutator<T> {

    default Element<?> getElement(PropsMapper.PropNode node) {
        input input = new input().text().name(node.getName()).id(node.getName()).value(node.getVal());
        if (Number.class.isAssignableFrom(node.getPd().getPropertyType())) {
            input.type("number ");
            System.out.println("Number: " + node.getName());
        } else if (node.getPd().getPropertyType().isPrimitive()) {
            input.type("number");
            System.out.println("Primitive: " + node.getName());
        } else {
            System.out.println("Not number: " + node.getName());
        }
        return input;
    }

    default List<Element<?>> inputs(T t) {
        return
                new PropsMapper()
                        .props(t, true)
                        .stream()
                        .map(pd -> getElement(pd))
                        .collect(toList());
    }

    default T setProps(T target, Map<String, String> req) {
        return new PropsMapper().setProps(target, req);
    }

    default T create(T t) {
        throw new UnsupportedOperationException("create");
    }

    default T update(T t) {
        throw new UnsupportedOperationException("update");
    }

    default void deleteById(String t) {
        throw new UnsupportedOperationException("delete");
    }

}
