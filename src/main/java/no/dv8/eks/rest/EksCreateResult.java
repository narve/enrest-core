package no.dv8.eks.rest;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XFunction;
import no.dv8.xhtml.generation.support.Element;

import java.util.function.Predicate;

public class EksCreateResult implements Predicate<Exchange>, XFunction<Exchange, Element<?>> {
    final EksResources resources;

    public EksCreateResult(EksResources resources) {
        this.resources = resources;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isCreateResult(x.getFullPath());
    }

    @Override
    public Element<?> apply(Exchange exchange) throws Exception {
        Element<?> obj;
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        Resource r = resources.locateByName(itemClass);
        obj = new EksCreateForms(resources).executeCreate(r, exchange.req);
        return obj;    }
}
