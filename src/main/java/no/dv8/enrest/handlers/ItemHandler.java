package no.dv8.enrest.handlers;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Resource;

import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class ItemHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final ResourceRegistry resources;

    public ItemHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.getPaths().isItem(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        return handleIt(exchange);
    }

    public <T> Exchange handleIt( Exchange exchange ) {
        String itemClass = resources.getPaths().type(exchange.getFullPath());
        String itemId = resources.getPaths().id(exchange.getFullPath());
        Resource<T> resource = (Resource<T>) resources.getByName(itemClass);
        Optional<T> item = resource.locator().apply(itemId);
        if (!item.isPresent()) {
            throw new IllegalArgumentException("Not found: " + itemClass + "#" + itemId);
        }

        switch (exchange.req.getMethod().toUpperCase()) {
            case "GET":
                return exchange.withEntity(item.get());
            case "POST":
            case "PUT":
                return exchange.withEntity(resource.updater().update(item.get()));
            case "DELETE":
                resource.updater().deleteById(itemId);
                return exchange.withEntity(null).withStatus(HttpURLConnection.HTTP_NO_CONTENT );
//                return exchange.withEntity(null).withStatus(HttpURLConnection.HTTP_NO_CONTENT );

            default:
                throw new UnsupportedOperationException(exchange.toString());
        }
    }

}
