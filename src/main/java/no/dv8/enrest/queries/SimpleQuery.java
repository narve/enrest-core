package no.dv8.enrest.queries;

import no.dv8.enrest.semantic.Names;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class SimpleQuery<T> implements QueryResource {
    private final Function<String, Collection<T>> func;
    private final String rel;

    public SimpleQuery(String rel, Function<String, Collection<T>> func ) {
        this.rel = rel;
        this.func = func;
    }

    @Override
    public String getRel() {
        return rel;
    }

    @Override
    public Collection<?> query(HttpServletRequest req) {
        String term = req.getParameter(Names.search.toString());
        return func.apply(term);
    }

    @Override
    public List<Parameter> params() {
        return asList(
          new Parameter("search", String.class.getSimpleName(), "text", null)
        );
    }
}
