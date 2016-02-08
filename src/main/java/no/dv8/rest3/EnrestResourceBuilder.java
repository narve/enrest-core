package no.dv8.rest3;

import no.dv8.rest2.framework.Link;
import no.dv8.rest2.framework.Request;

import javax.servlet.ServletRequest;
import java.util.*;
import java.util.function.Function;


public class EnrestResourceBuilder<From, To> {

    boolean single;
    Enrest enrest;
    String pathPattern, method, name, ref;
    Class<From> from;
    Class<To> to;
    Function<ServletRequest, From> reqParser;
    Function<From, List<To>> handler;
    List<Parameter> queryParams = new ArrayList<>();
    List<Parameter> pathParams = new ArrayList<>();
    List<Parameter> bodyParams = new ArrayList<>();
    Function<To, List<Link>> linker = to -> new ArrayList<>();

    public EnrestResourceBuilder(Enrest enrest, boolean single, Class<From> from, Class<To> to) {
        this.enrest = enrest;
        this.from = from;
        this.to = to;
        this.single = single;
    }

    public EnrestResourceBuilder<From, To>  self() {
        return this;
    }

    public EnrestResourceBuilder<From, To> queryParam(String s) {
        this.queryParams.add( Parameter.builder().name(s).build());
        return self();
    }

    public EnrestResourceBuilder<From, To> queryParam(String s, String ht) {
        this.queryParams.add( Parameter.builder().name(s).htmlType(ht).build());
        return self();
    }

    public EnrestResourceBuilder<From, To> pathParam(String s) {
        this.pathParams.add( Parameter.builder().name(s).build());
        return self();
    }

    public EnrestResourceBuilder<From, To> handler(Function<From, List<To>> func) {
        this.handler = func;
        return self();
    }

    public EnrestResourceBuilder<From, To> reqParser(Function<ServletRequest, From> func) {
        this.reqParser = func;
        return self();
    }

    public EnrestResourceBuilder<From, To> method( String m ) {
        this.method = m;
        return self();
    }

    public EnrestResourceBuilder<From, To> name( String x ) {
        this.name = x;
        return self();
    }

    public EnrestResourceBuilder<From, To> linker(Function<To, List<Link>> linker) {
        this.linker = linker;
        return self();
    }

    public EnrestResource build() {
        String uuid = UUID.randomUUID().toString();
        pathPattern = pathPattern == null ? uuid : pathPattern;
        name = name == null ? uuid : name;
        ref = ref == null ? uuid : ref;

        return new EnrestResource( single, method, pathPattern, from, to, reqParser, handler, name, ref, queryParams, pathParams, bodyParams, linker );
    }

    public EnrestResource buildAndRegister() {
        EnrestResource r = build();
        return enrest.register( r );
    }


    public EnrestResourceBuilder<From, To> jsonBodyParam() {
        bodyParams.add( Parameter.builder().name( "body" ).build());
        return self();
    }

    public EnrestResourceBuilder<From, To> pathPattern(String s) {
        pathPattern = s;
        return self();
    }

}
