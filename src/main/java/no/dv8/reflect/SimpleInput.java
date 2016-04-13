package no.dv8.reflect;


import no.dv8.xhtml.generation.elements.input;

import java.beans.PropertyDescriptor;
import java.util.function.Function;

public class SimpleInput implements Function<PropertyDescriptor, input> {
    @Override
    public input apply(PropertyDescriptor pd) {
        return new input().text().name(pd.getName()).id(pd.getName());
    }
}
