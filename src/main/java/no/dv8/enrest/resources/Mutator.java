package no.dv8.enrest.resources;

import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface Mutator<T> {

    List<Element<?>> inputs(T t);

    T create( T t );

    T setProps(T target, HttpServletRequest req);

    T update( T t );

}
