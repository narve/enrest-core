package no.dv8.utils;

import lombok.extern.slf4j.Slf4j;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class Props {

//    ResourceRegistry resources;

    public Props() {
//        this.resources = resources;
    }

    public List<PropertyDescriptor> all(Class<?> clz) {
        try {
            PropertyDescriptor[] pda = Introspector.getBeanInfo(clz).getPropertyDescriptors();
            return asList(pda)
              .stream()
              .filter(pd -> !pd.getName().equals("class"))
              .collect(toList());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T setProps(T t, Map<String, String> values) {
        Map<String, PropertyDescriptor> pdMap =
          all(t.getClass()).stream().collect(toMap(PropertyDescriptor::getName, identity()));

        values.entrySet().stream()
          .forEach(me -> setProp(pdMap.get(me.getKey()), me.getValue(), t));
        return t;
    }

    public <T> void setProp(PropertyDescriptor pd, String value, T t) {
        if (value == null || value.trim().isEmpty())
            return;
        if (pd.getName().equals("id"))
            return;
        String msg = format("setting %s to '%s' on target '%s'", pd.getName(), value, t);
        log.info(msg);
        try {
            Object val = coerce(value, pd);
            pd.getWriteMethod().invoke(t, val);
        } catch (ClassCastException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error in " + msg, e);
        }
    }

    public Object coerce(String value, PropertyDescriptor pd) {
        log.debug( "Coercing {} to {} ({})", value, pd.getName(), pd.getPropertyType());
        if( value == null ) return null;
        if (pd.getPropertyType().equals(Long.TYPE) || pd.getPropertyType().equals( Long.class))
            return Long.parseLong(value);

//        Optional<Resource> res = resources.resources().stream().filter(r -> r.clz().getSimpleName().equalsIgnoreCase(pd.getPropertyType().getSimpleName())).findFirst();
//        if (res.isPresent()) {
//            log.debug("Locating bean for class {} id {}", pd.getPropertyType().getSimpleName(), value);
//            Object val = res.get().locator().apply(value);
//            log.info("Located bean for class {} id {}: {}", pd.getPropertyType().getSimpleName(), value, val);
//            if (val == null)
//                throw new IllegalArgumentException(format("Unable to locate %s by ref '%s'", pd.getPropertyType().getSimpleName(), value));
//            return val;
//        }

        return value;
    }

    public <T> T createAndSetProps(Class<T> tclz, Map<String, String> values) {
        try {
            T t = tclz.newInstance();
            setProps(t, values);
            return t;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> single(Map<String, String[]> stringMap) {
        return stringMap
          .entrySet()
          .stream()
          .peek(this::assertLength)
          .collect(toMap(Map.Entry::getKey, e -> e.getValue()[0]));
    }

    public void assertLength(Map.Entry<String, String[]> e) {
        if (e.getValue().length != 1)
            throw new IllegalArgumentException(e.getValue().length + " values for '" + e.getKey() + "': " + Arrays.toString( e.getValue()));
    }

}
