package no.dv8.enrest.queries;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;

public interface QueryResource {
    String getRel();
    Collection<?> query(HttpServletRequest req);
    default List<Parameter> params() {
        return emptyList();
    }
}
