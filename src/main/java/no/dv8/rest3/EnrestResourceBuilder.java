package no.dv8.rest3;

import no.dv8.rest2.framework.Request;

import javax.servlet.ServletRequest;
import java.util.*;
import java.util.function.Function;


public class EnrestResourceBuilder<From, To> {

    Enrest enrest;
    String pathPattern, method, name, ref;
    Class<From> from;
    Class<To> to;
    Function<ServletRequest, From> reqParser;
    Function<From, To> handler;
    List<Parameter> queryParams = new ArrayList<>();
    List<Parameter> pathParams = new ArrayList<>();
    List<Parameter> bodyParams = new ArrayList<>();

    public EnrestResourceBuilder(Enrest enrest, Class<From> from, Class<To> to) {
        this.enrest = enrest;
        this.from = from;
        this.to = to;
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

    public EnrestResourceBuilder<From, To> handler(Function<From, To> func) {
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

    public EnrestResource build() {
        String uuid = UUID.randomUUID().toString();
        pathPattern = pathPattern == null ? uuid : pathPattern;
        name = name == null ? uuid : name;
        ref = ref == null ? uuid : ref;

        return new EnrestResource( method, pathPattern, from, to, reqParser, handler, name, ref, queryParams, pathParams, bodyParams );
    }

    public Enrest buildAndRegister() {
        EnrestResource r = build();
        return enrest.register( r );
    }


    public EnrestResourceBuilder<From, To> jsonBodyParam() {
        bodyParams.add( Parameter.builder().name( "body" ).build());
        return self();
    }
}
