package no.dv8.enrest.queries;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public interface QueryResource {
    String getRel();
    Collection<?> query(HttpServletRequest req);
}
