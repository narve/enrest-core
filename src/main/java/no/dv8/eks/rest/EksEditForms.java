package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class EksEditForms implements Predicate<Exchange>, UnaryOperator<Exchange> {

    private final EksResources resources;

    public EksEditForms(EksResources resources) {
        this.resources = resources;
    }

    EksCreateForms forms() {
        return new EksCreateForms(resources);
    }


    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isEditForm(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        String itemId = resources.urlCreator.id(exchange.getFullPath());
        Resource<?> resource = resources.getByName(itemClass);
        Object item = resource.locator().apply(itemId).get();
        return exchange.withEntity(forms().editForm(resource.updater(), item));
    }
}
