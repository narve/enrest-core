package no.dv8.reflect;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.rest.EksApi;
import no.dv8.enrest.resources.Resource;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
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
        Map<String, PropertyDescriptor> pdMap =
          all(t.getClass()).stream().collect( toMap( PropertyDescriptor::getName, identity()));

        values.entrySet().stream()
          .forEach( me -> setProp( pdMap.get( me.getKey()), me.getValue(),t ) );
        return t;
    }

    private <T> void setProp(PropertyDescriptor pd, String value, T t) {
        if( value == null || value.trim().isEmpty() )
            return;
        if( pd.getName().equals( "id" ) )
            return;
        String msg = format( "setting %s to '%s' on target '%s'", pd.getName(), value, t );
        log.info( msg );
        try {
            Object val = value;
            if( pd.getPropertyType().equals( Long.TYPE ) )
                val = Long.parseLong(value);


            Optional<Resource> res = EksApi.resources().stream().filter(r -> r.clz().getSimpleName().equalsIgnoreCase(pd.getPropertyType().getSimpleName())).findFirst();
            if( res.isPresent() ) {
                log.info( "Locating bean for class {} id {}", pd.getPropertyType().getSimpleName(), value );
                Object byId = res.get().locator().getById(value);
                val = byId;
                log.info( "Located bean for class {} id {}: {}", pd.getPropertyType().getSimpleName(), value, val );
                if( val == null )
                    throw new IllegalArgumentException( format( "Unable to locate %s by ref '%s'", pd.getPropertyType().getSimpleName(), value ) );
            }

            pd.getWriteMethod().invoke( t, val );
        } catch (ClassCastException | IllegalAccessException |InvocationTargetException e) {
            throw new RuntimeException( "Error in " + msg, e );
        }
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
