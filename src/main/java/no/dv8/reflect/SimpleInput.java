package no.dv8.reflect;


import no.dv8.eks.model.ModelBase;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.option;
import no.dv8.xhtml.generation.support.Custom;
import no.dv8.xhtml.generation.support.Element;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;

import static java.util.Arrays.asList;

public class SimpleInput<T>
  implements BiFunction<PropertyDescriptor, T, Element<?>> {
    @Override
    public Element<?> apply(PropertyDescriptor pd, T t) {
        try {
            Object val = t == null ? null : pd.getReadMethod().invoke(t);
            if (val instanceof ModelBase) {
                val = ((ModelBase) val).getId();


                Custom c = new Custom("datalist").id(pd.getPropertyType().getSimpleName());
                c.add(asList(
                  new option().set("value", "33").add("Narve Sætre"),
                  new option().set("value", "1234234").add("NOT Narve Sætre")
                ));

                input x = new input().text().name(pd.getName()).id(pd.getName()).set("list", pd.getPropertyType().getSimpleName());

                return new div().add(c).add(x).set("name", pd.getName()).id(pd.getName());

            }
            return new input().text().name(pd.getName()).id(pd.getName()).value(val);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
