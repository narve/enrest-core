package no.dv8.eks.rest;

import no.dv8.enrest.Exchange;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.elements.h1;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class EksNotFound implements Predicate<Exchange>, UnaryOperator<Exchange> {
    @Override
    public boolean test(Exchange exchange) {
        return true;
    }

    @Override
    public Exchange apply(Exchange x) {
        return x.withEntity(new div().add(new h1("404 Not found: " + x.getFullPath())));
    }
}
