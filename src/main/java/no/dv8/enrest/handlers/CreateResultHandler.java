package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Resource;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class CreateResultHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {
    final ResourceRegistry resources;

    public CreateResultHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isCreateResult(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        Object obj;
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        Resource r = resources.getByName(itemClass);
        obj = new CreateFormHandler(resources).executeCreate(r, exchange.req);
        return exchange.withEntity(obj);    }
}
