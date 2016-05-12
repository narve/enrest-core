package no.dv8.enrest.props;

import javafx.util.Pair;
import no.dv8.reflect.Props;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropsMapper {

    static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    public Map<String, String> toMap(Object t, String prefix) {
        return
          new Props()
            .all(t.getClass())
            .stream()
            .flatMap(pd -> propToMap(pd, t))
            .filter( p -> p.getValue() != null )
            .collect(Collectors.toMap(p -> prefix + p.getKey(), p -> p.getValue(), throwingMerger(), TreeMap::new));
    }

    Stream<Pair<String, String>> propToMap(PropertyDescriptor pd, Object t) {
        if (pd.getPropertyType().isPrimitive() || pd.getPropertyType().getPackage().getName().startsWith("java"))
            return Stream.of(new Pair<>(pd.getName(), getValue(pd, t)));

        try {
            Object nested = pd.getReadMethod().invoke(t);
            return toMap(nested, pd.getName() + ".")
              .entrySet()
              .stream()
              .map(me -> new Pair<>(me.getKey(), me.getValue()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    String getValue(PropertyDescriptor pd, Object t) {
        try {
            Object val = pd.getReadMethod().invoke(t);
            return val == null ? null : val.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromMap(Class<T> clz, Map<String, String> vals) {
        return new Props().createAndSetProps(clz, vals);
    }
}
