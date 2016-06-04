package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class QueryResultHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {
    final ResourceRegistry resources;

    public QueryResultHandler(ResourceRegistry resources) {
        this.resources = resources;
    }


    @Override
    public boolean test(Exchange x) {
        return resources.getPaths().isQueryResult(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        Collection<?> objects = resources.executeQuery(this.resources.getPaths().queryName(exchange.getFullPath()), exchange.req);
        ul ul = new ul();
        objects.forEach(o -> ul.add(new li().add(linkToObject(o))));
        return exchange.withEntity(ul);
    }

    private a linkToObject(Object u) {
        return new a(u.toString()).href(resources.getPaths().viewItem(resources.itemClass(u), resources.itemId(u)));
    }
}
