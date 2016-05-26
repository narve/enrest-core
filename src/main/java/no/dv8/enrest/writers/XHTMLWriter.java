package no.dv8.enrest.writers;

import no.dv8.eks.rest.EksHTML;
import no.dv8.enrest.Exchange;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toList;

public class XHTMLWriter implements UnaryOperator<Exchange> {

    public <T> Element<?> toElement(T item, List<a> links) {

        div d = new div();
        d.add(new XHTMLSerialize<>().generateElement(item, 1));
        d.add(new div().clz("links").add(
          links.stream().map(l -> new li().add(l)).collect(toList())
        ));
        return d;
    }


    @Override
    public Exchange apply(Exchange exchange) {
        Object entity = exchange.getEntity();
        Element<?> result;
        if (entity instanceof Element) {
            result = (Element<?>) entity;
        } else {
            result = toElement(entity, exchange.getLinks());
        }
        String title = exchange.req.getPathInfo();

        exchange.res.setContentType("text/html");
        exchange.res.setCharacterEncoding("utf-8");
        try {
            PrintWriter writer = exchange.res.getWriter();
            writer.print(EksHTML.complete(result, title).toString());
            writer.close();
            return exchange;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ;
}
