package no.dv8.enrest.creators;

import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CreatorResource {

    List<Element<?>> inputs();

    Element handle(HttpServletRequest req);

    String getName();
}
