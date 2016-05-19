package no.dv8.enrest.props;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import no.dv8.eks.rest.EksResources;
import no.dv8.reflect.Props;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PropsMapper {

    static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

//    final EksResources resources;

    public PropsMapper() {
//        this.resources = resources;
    }

    public List<PropNode> props(Object t, boolean includeNull ) {
        return new Props()
          .all( t.getClass() )
          .stream()
          .flatMap( pd -> propToMap(pd, t, "", includeNull))
          .sorted()
          .collect( toList() );
    }

    Stream<PropNode> propToMap(PropertyDescriptor descriptor, Object t, String prefix, boolean includeNull) {
        Object val = getValue(descriptor, t);
        if (val == null && !includeNull)
            return Stream.empty();

        if (isSimple(descriptor)) {
            return Stream.of(new PropNode(prefix + descriptor.getName(), descriptor, getValue(descriptor, t)));
        }

        try {
            Object nested = val == null ? descriptor.getPropertyType().newInstance() : val;
            return new Props()
              .all(descriptor.getPropertyType())
              .stream()
              .peek ( pd -> getValue(pd, nested ) )
              .flatMap(subProp -> propToMap(subProp, nested, prefix + descriptor.getName() + ".", false ) );
        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException e) {
            throw new RuntimeException("Error with " + prefix + descriptor.getName() + " " + descriptor.getPropertyType(), e);
        }
    }

    private boolean isSimple(PropertyDescriptor pd) {
        return pd.getPropertyType().isPrimitive() || pd.getPropertyType().getPackage().getName().startsWith("java");
    }

    public Object getValue(PropertyDescriptor pd, Object t) {
        try {
            return pd.getReadMethod().invoke(t);
        } catch (IllegalAccessException | InvocationTargetException |IllegalArgumentException e) {
            throw new RuntimeException("Error with " + pd.getName() + " " + pd.getPropertyType() + "/" + t.getClass(), e);
        }
    }

    public Object setValue(PropNode pn, Object target, String stringVal) {
        try {
            Object val = new Props().coerce( stringVal, pn.getPd() );
            return pn.getPd().getWriteMethod().invoke(target, val);
        } catch (IllegalAccessException | InvocationTargetException |IllegalArgumentException e) {
            String s = "Error with property " + pn.getName();
            s += "; pd-type=" + pn.getPd().getPropertyType().getName();
            s += "; target-type=" + target.getClass().getName();
            throw new RuntimeException(s, e);
        }
    }

    public <T> T setProps(T target, Map<String, String> req) {
        List<PropNode> props = props(target, true);
        for( PropNode pn: props ) {
            if( !canSet( pn ) )
                continue;
            String parameter = req.get(pn.getName());
            setValue( pn, target, parameter );
        }
        return target;
    }

    private boolean canSet(PropNode pn) {
        if( pn.getName().equals( "id" ) )
            return false;
        if( Collection.class.isAssignableFrom( pn.getPd().getPropertyType() ) )
            return false;
        if( pn.getPd().getWriteMethod() == null )
            return false;
        return true;

    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Value
    public static class PropNode implements Comparable<PropNode>{
        String name;
        PropertyDescriptor pd;
        Object val;

        @Override
        public int compareTo(PropNode o) {
            return this.name.compareTo( o.name);
        }
    }
}
