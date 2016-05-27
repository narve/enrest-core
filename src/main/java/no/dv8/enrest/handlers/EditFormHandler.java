package no.dv8.enrest.handlers;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.elements.form;
import no.dv8.enrest.semantic.Rels;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class EditFormHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    private final ResourceRegistry resources;

    public EditFormHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    CreateFormHandler forms() {
        return new CreateFormHandler(resources);
    }

    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isEditForm(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        String itemId = resources.urlCreator.id(exchange.getFullPath());
        Resource<?> resource = resources.getByName(itemClass);
        Object item = resource.locator().apply(itemId).get();
        Mutator updater = resource.updater();
        form f = forms().getForm(itemClass, "edit", updater.inputs(item), "post");
        f.action(resources.urlCreator.viewItem(resources.itemClass(item), resources.itemId(item)));
        return exchange.withEntity(f);
    }
}
