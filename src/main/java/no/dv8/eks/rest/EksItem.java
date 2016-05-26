package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XFunction;
import no.dv8.functions.XUnaryOperator;
import no.dv8.xhtml.generation.support.Element;

import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
public class EksItem implements Predicate<Exchange>, XUnaryOperator<Exchange> {

    final EksResources resources;

    public EksItem(EksResources resources) {
        this.resources = resources;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isItem(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) throws Exception {
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        String itemId = resources.urlCreator.id(exchange.getFullPath());
        Resource<?> resource = resources.locateByName(itemClass);
        Optional<?> item = resource.locator().apply(itemId);
        if (!item.isPresent()) {
            throw new IllegalArgumentException("Not found: " + itemClass + "#" + itemId);
        }

        switch (exchange.req.getMethod().toUpperCase()) {
            case "GET":
                return exchange.withEntity(resources.toElement(item.get()));
            case "POST":
            case "PUT":
                return exchange.withEntity(resources.executeUpdate(resource, item.get(), exchange.req));
            default:
                throw new UnsupportedOperationException(exchange.toString());
        }
    }


}
