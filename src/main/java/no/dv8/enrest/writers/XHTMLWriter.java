package no.dv8.enrest.writers;

import no.dv8.eks.rest.EksHTML;
import no.dv8.enrest.Exchange;
import no.dv8.xhtml.generation.support.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.UnaryOperator;

public class XHTMLWriter implements UnaryOperator<Exchange> {
    @Override
    public Exchange apply(Exchange exchange) {
        Element<?> result = (Element<?>) (exchange.getEntity());
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
