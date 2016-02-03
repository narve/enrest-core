package no.dv8.rest3;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.servlet.ServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
//@Builder
public class EnrestResource<From, To> {
    @Getter
    String method;
    @Getter
    String path;
    @Getter
    String name;
    @Getter
    String reference;
    @Getter
    Class<From> from;
    @Getter
    Class<To> to;
    @Getter
    Function<From, To> handler;
    @Getter
    Function<ServletRequest, From> reqParser;

    List<Parameter> queryParams;
    List<Parameter> pathParams;
    List<Parameter> bodyParams;

    public List<Parameter> getQueryParams() {
        return queryParams;
    }

    public List<Parameter> getPathParams() {
        return pathParams;
    }

    public List<Parameter> getBodyParams() {
        return bodyParams;
    }

    public EnrestResource(String m, String p, Class<From> f, Class<To> t, Function<ServletRequest, From> rp, Function<From, To> h, String n, String r, List<Parameter> qp, List<Parameter> pp, List<Parameter> bodyParams ) {
        Objects.requireNonNull(h, "Required: Handler" );
        Objects.requireNonNull(p, "Required: Path" );
        Objects.requireNonNull(f, "Required: From" );
        Objects.requireNonNull(t, "Required: To" );
        Objects.requireNonNull(m, "Required: method" );
        Objects.requireNonNull(m, "Required: name" );
        Objects.requireNonNull(m, "Required: reference" );
        Objects.requireNonNull(m, "Required: queryParams" );
        this.method = m;
        this.from = f;
        this.to = t;
        this.path = p;
        this.reqParser = rp;
        this.handler = h;
        this.name = n;
        this.reference = r;
        this.queryParams = qp;
        this.pathParams = pp;
        this.bodyParams = bodyParams;
    }

}
