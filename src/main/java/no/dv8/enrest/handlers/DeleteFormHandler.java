package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.elements.form;
import no.dv8.xhtml.generation.elements.input;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class DeleteFormHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    private final ResourceRegistry resources;

    public DeleteFormHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    @Override
    public Exchange apply(Exchange exchange) {
        String itemClass = resources.getPaths().type(exchange.getFullPath());
        String itemId = resources.getPaths().id(exchange.getFullPath());
        Resource<?> resource = resources.getByName(itemClass);
        form f = new form()
          .action( resources.getPaths().deleteFormResult( itemClass, itemId ) )
          .post()
          .add(
            new input().text().readonly().value( itemId )
          ).add( new input().submit() );
        return exchange.withOutEntity(f);
    }

    @Override
    public boolean test(Exchange x) {
        return resources.getPaths().isDeleteForm(x.getFullPath());
    }
}
