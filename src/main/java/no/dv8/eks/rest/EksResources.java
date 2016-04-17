package no.dv8.eks.rest;

import no.dv8.enrest.model.Link;
import no.dv8.enrest.mutation.Mutator;
import no.dv8.enrest.mutation.Resource;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import static java.util.stream.Collectors.toList;

public class EksResources {

    final String basePath;

    public EksResources(String basePath) {
        this.basePath = basePath;
    }

    EksForms eksForms() {
        return new EksForms(basePath);
    }

    public static final String pathToResource = "view-resource";
    public static final String editPathToResource = "edit-resource";

    public String viewUrlForItem(Object o ) {
        return basePath + "/" + pathToResource + "/" + itemClass( o ) + "/" + itemId( o );
    }

    public String editUrlForItem(Object o ) {
        return basePath + "/" + editPathToResource + "/" + itemClass( o ) + "/" + itemId( o );
    }

    public static String itemId(Object o) {
        try {
            return o.getClass().getMethod( "getId").invoke( o).toString();
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public static String itemClass(Object o) {
        return o.getClass().getSimpleName().toLowerCase();
    }

    public Element<?> itemToElement(String substring) {
        return toElement( getItem( substring));
    }

    public static Object getItem(String substring) {
        String itemClass = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        return getItem(itemClass, itemId);
    }

    public <T> Element<?> toElement(T item) {
        Resource<T> r = getResource(item.getClass().getSimpleName());
        div d = new div();
        d.add(new XHTMLSerialize<>().generateElement(item, 1));
        d.add( new div().clz( "links" ).add(
          new a( "Edit " + item.toString()).href( editUrlForItem(item)).rel( "edit")
        ).add( r.linker().links(item).stream().map( this::linkToElement).collect( toList()))
        );
        return d;
    }

    private Element<?> linkToElement(Element<?> l) {
        return l;
//        return new a().href(String.valueOf(l.getTarget())).rel( "therel").add( l.toString() );
    }

    public static Object getItem(String itemType, String itemId) {
        Resource<?> resource = getResource(itemType);
        return resource.locator().getById(itemId);
    }

    private static <T> Resource<T> getResource(String itemType) {
        return EksApi.resources().stream().filter(r -> r.getName().equalsIgnoreCase( itemType)).findFirst().get();
    }

    public Element<?> executeUpdate(String substring, HttpServletRequest req) {
        String itemClass = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        Object item = getItem(substring);
        Mutator resourceMutatorResource = eksForms().locateByClz(itemClass);
        Object q = resourceMutatorResource.setProps(item, req);
        resourceMutatorResource.update(q);
        return toElement(q);
    }
}
