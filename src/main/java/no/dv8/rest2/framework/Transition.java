package no.dv8.rest2.framework;

import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
@Getter()
public class Transition<F, T> {

    Class<F> from;
    Class<T> to;
    List<Class<?>> linkedFrom = new ArrayList<>();
    String name;

    Function<F, T> action;
    String url;

    @XmlTransient
    public Function<F,T> getAction() {
        return action;
    }


    public Function<T, String> urlBuilder = t -> (""+t.hashCode());



    public String toString() {
//        return linkedFrom.stream().map(Class::getName).collect(toList()) + " =[" + name + "]> " + from.getTypeName();
        return name == null ? ( to == null ? "N/A" : to.getSimpleName() ) : name;
    }

}
