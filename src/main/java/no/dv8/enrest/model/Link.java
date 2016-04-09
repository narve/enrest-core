package no.dv8.enrest.model;


import lombok.Data;
import no.dv8.enrest.EnrestResource;
import no.dv8.enrest.model.Parameter;

import java.util.ArrayList;
import java.util.List;

@Data
public class Link {

    String rel;

    EnrestResource<?, ?> target;

    List<Parameter> parameters = new ArrayList<>();

}
