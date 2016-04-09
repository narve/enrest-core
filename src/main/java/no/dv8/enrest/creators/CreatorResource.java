package no.dv8.enrest.creators;

import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CreatorResource<T> {

    List<Element<?>> inputs(T t);

    T handle(HttpServletRequest req);

    String getName();

    default boolean isIdempotent() {
        return false;
    }
}
