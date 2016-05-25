package no.dv8.eks.rest;

import no.dv8.enrest.Exchange;
import no.dv8.functions.XFunction;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.elements.h1;
import no.dv8.xhtml.generation.support.Element;

import java.util.function.Predicate;

public class EksNotFound implements Predicate<Exchange>, XFunction<Exchange, Element<?>> {
    @Override
    public boolean test(Exchange exchange) {
        return true;
    }

    @Override
    public Element<?> apply(Exchange x) throws Exception {
        return new div().add(new h1("404 Not found: " + x.getFullPath()));
    }
}
