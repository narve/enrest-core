package no.dv8.enrest.handlers;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.elements.a;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;


@Slf4j
public class LinkHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final ResourceRegistry resources;

    public LinkHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    @Override
    public Exchange apply(Exchange exchange) {
        Objects.requireNonNull( exchange.getOutEntity(), "Cant handle links for NULL" );
        Object item = exchange.getOutEntity();
//        Resource resource = resources.locateByClz(item.getClass()).orElseThrow( () -> new UnsupportedOperationException("No resource for " + item.getClass()));
        Optional<Resource<?>> resourceOpt = resources.locateByClz(item.getClass());;
        if( !resourceOpt.isPresent() ) {
            log.info( "Unsupported element '" + item.getClass() + "', no links" );
            return exchange;
        }

        Resource resource = resourceOpt.get();
        List<a> links = resource.linker().links(item);

        for (a link : links) {
            Optional<Resource<?>> sub = resources.locateByClz(link.href().getClass());
            if (sub.isPresent()) {
                log.info("Converting link using {}", sub);
                switch (link.getAttributes().getOrDefault("rel", "").toString()) {
                    case "edit":
                        link.href(resources.getPaths().editForm(resources.itemClass(link.href()), resources.itemId(link.href())));
                        break;
                    case "self":
                        link.href(resources.getPaths().viewItem(resources.itemClass(link.href()), resources.itemId(link.href())));
                        break;
                    default:
                        log.warn( "Unsupported rel " + link.getAttributes().get( "rel"));
                        link.href(resources.getPaths().viewItem(resources.itemClass(link.href()), resources.itemId(link.href())));
                        break;
                }
            }
        }
        return exchange.withLinks( links );
    }

    @Override
    public boolean test(Exchange exchange) {
        return true;
    }
}
