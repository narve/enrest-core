package no.dv8.eks.rest;

import no.dv8.enrest.Exchange;
import no.dv8.functions.XFunction;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Element;

import java.util.Collection;
import java.util.function.Predicate;

public class EksQueryResults implements Predicate<Exchange>, XFunction<Exchange, Element<?>> {
    final EksResources resources;

    public EksQueryResults(EksResources resources) {
        this.resources = resources;
    }


    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isQueryResult(x.getFullPath());
    }

    @Override
    public Element<?> apply(Exchange exchange) throws Exception {
        Collection<?> objects = resources.executeQuery(this.resources.urlCreator.queryName(exchange.getFullPath()), exchange.req);
        ul ul = new ul();
        objects.forEach(o -> ul.add(new li().add(linkToObject(o))));
        return ul;
    }

    private a linkToObject(Object u) {
        return new a(u.toString()).href(resources.urlCreator.viewItem(resources.itemClass(u), resources.itemId(u)));
    }
}
