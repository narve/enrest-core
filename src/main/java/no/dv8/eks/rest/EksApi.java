package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.EksAlps;
import no.dv8.enrest.Exchange;
import no.dv8.functions.XFunction;
import no.dv8.utils.Forker;
import no.dv8.xhtml.generation.elements.p;
import no.dv8.xhtml.generation.support.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class EksApi implements XFunction<Exchange, Exchange> {

    final EksResources resources;

    public EksApi(EksResources resources) {
        Objects.requireNonNull(resources);
        this.resources = resources;
    }

    @Override
    public Exchange apply(Exchange exchange) throws IOException {

        Function<Exchange, Element<?>> forker = new Forker<Exchange, Element<?>>()
          .add("test", x -> x.getFullPath().endsWith("/test"), x -> new p("test"))
          .add(new EksAlps())
          .add(new EksIndex(this.resources))
          .add(new EksItem(this.resources))
          .add(new EksEditForms(resources))
          .add(new EksQueryForms(resources))
          .add(new EksQueryResults(resources))
          .add(new EksCreateForms(resources))
          .add(new EksCreateResult(resources))
          .add(new EksNotFound())
          .forker();

        Element<?> result = forker.apply(exchange);
        String title = exchange.req.getPathInfo();

        exchange.res.setContentType("text/html");
        exchange.res.setCharacterEncoding("utf-8");
        PrintWriter writer = exchange.res.getWriter();
        writer.print(EksHTML.complete(result, title).toString());
        writer.close();
        return exchange;
    }

}
