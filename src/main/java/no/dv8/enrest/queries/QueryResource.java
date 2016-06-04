package no.dv8.enrest.queries;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public interface QueryResource {
    String getRel();
    Collection<?> query(Map<String, String[]> parameters);
    default List<Parameter> params() {
        return emptyList();
    }
}
