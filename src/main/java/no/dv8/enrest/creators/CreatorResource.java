package no.dv8.enrest.creators;

import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CreatorResource<T> {

    List<Element<?>> inputs(T t);

    T create( T t );

    T update( T t );

    T setProps(T target, HttpServletRequest req);

    String getName();

    Class<T> clz();

    default boolean isIdempotent() {
        return false;
    }
}
