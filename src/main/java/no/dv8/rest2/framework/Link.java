package no.dv8.rest2.framework;


import lombok.Data;
import no.dv8.rest3.EnrestResource;
import no.dv8.rest3.Parameter;

import java.util.ArrayList;
import java.util.List;

@Data
public class Link {

    String rel;

    EnrestResource<?, ?> target;

    List<Parameter> parameters = new ArrayList<>();

}
