package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XUnaryOperator;

import java.util.function.Predicate;

@Slf4j
public class EksEditForms implements Predicate<Exchange>, XUnaryOperator<Exchange> {

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
    public Exchange apply(Exchange exchange) throws Exception {
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        String itemId = resources.urlCreator.id(exchange.getFullPath());
        Resource<?> resource = resources.locateByName(itemClass);
        Object item = resource.locator().apply(itemId).get();
        return exchange.withEntity(forms().editForm(resource.updater(), item));
    }
}
