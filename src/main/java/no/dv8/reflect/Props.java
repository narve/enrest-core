package no.dv8.reflect;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Props {

    public List<PropertyDescriptor> all(Class<?> clz ) {
        try {
            PropertyDescriptor[] pda = Introspector.getBeanInfo(clz).getPropertyDescriptors();
        return asList(pda)
                .stream()
                .filter( pd -> !pd.getName().equals( "class" ))
                .collect( toList() );
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T setProps( T t, Map<String,String> values ) {
        return t;
    }

    public <T> T createAndSetProps( Class<T> t, Map<String,String> values ) {
        try {
            return t.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> single(Map<String, String[]> stringMap) {
        return stringMap
                .entrySet()
                .stream()
                .collect( Collectors.toMap( Map.Entry::getKey, e -> e.getValue()[0] ) );
        // return new HashMap<String, String[]>( stringMap )
        //        .replaceAll( (k,v) -> v[0] );
    }
}
