package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.utils.Props;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class EksItem implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final EksResources resources;

    public EksItem(EksResources resources) {
        this.resources = resources;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isItem(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        String itemId = resources.urlCreator.id(exchange.getFullPath());
        Resource<?> resource = resources.locateByName(itemClass);
        Optional<?> item = resource.locator().apply(itemId);
        if (!item.isPresent()) {
            throw new IllegalArgumentException("Not found: " + itemClass + "#" + itemId);
        }

        switch (exchange.req.getMethod().toUpperCase()) {
            case "GET":
//                return exchange.withEntity(resources.toElement(item.get()));
                return exchange.withEntity(item.get());
            case "POST":
            case "PUT":
                return exchange.withEntity(executeUpdate(resource, item.get(), exchange.req));
            default:
                throw new UnsupportedOperationException(exchange.toString());
        }
    }

    public Object executeUpdate(Resource<?> resource, Object item, HttpServletRequest req) {
        Mutator mutator = resource.creator();
        Map<String, String> vals = new Props().single(req.getParameterMap());
        Object q = mutator.setProps(item, vals);
        q = mutator.update(q);
//        return toElement(q);
        return q;
    }


}
