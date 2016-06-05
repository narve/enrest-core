package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Resource;

import java.net.HttpURLConnection;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class DeleteByFormHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    private final ResourceRegistry resources;

    public DeleteByFormHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    @Override
    public Exchange apply(Exchange exchange) {
        return handleIt(exchange);
    }

    public <T> Exchange handleIt( Exchange exchange ) {
        String itemClass = resources.getPaths().type(exchange.getFullPath());
        String itemId = resources.getPaths().id(exchange.getFullPath());
        Resource<T> resource = (Resource<T>) resources.getByName(itemClass);
        resource.deleter().accept(itemId);
        return exchange.withStatus(HttpURLConnection.HTTP_NO_CONTENT );
    }

    @Override
    public boolean test(Exchange x) {
        return resources.getPaths().isDeleteFormResult( x.getFullPath() );
    }
}
