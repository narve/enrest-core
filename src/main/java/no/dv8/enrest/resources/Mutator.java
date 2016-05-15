package no.dv8.enrest.resources;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.props.PropsMapper;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.stream.Collectors.toList;

public interface Mutator<T> {

    default Element<?> getElement(PropsMapper.PropNode node) {
        input input = new input().text().name(node.getName()).id(node.getName()).value(node.getVal());
        if( Number.class.isAssignableFrom(node.getPd().getPropertyType()) ) {
            input.type("number ");
            System.out.println("Number: " + node.getName());
        } else if( node.getPd().getPropertyType().isPrimitive() )  {
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

    default T setProps(T target, HttpServletRequest req) {
        return new PropsMapper().setProps( target, req );
    }

    default T create(T t) {
        throw new UnsupportedOperationException("create");
    }

    default T update(T t) {
        throw new UnsupportedOperationException("update");
    }

}
