package no.dv8.reflect;


import no.dv8.eks.model.ModelBase;
import no.dv8.xhtml.generation.elements.input;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;

public class SimpleInput<T>
  implements BiFunction<PropertyDescriptor, T, input> {
    @Override
    public input apply(PropertyDescriptor pd, T t) {
        try {
            Object val = t == null ? null : pd.getReadMethod().invoke(t);
            if( val instanceof ModelBase ) {
                val = ((ModelBase)val).getId();
            }
            return new input().text().name(pd.getName()).id(pd.getName()).value(val);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
