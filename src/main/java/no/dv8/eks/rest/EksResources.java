package no.dv8.eks.rest;

import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
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

    public Element<?> toElement(Object item) {
        div d = new div();
        if( true ) {
            d.add(new XHTMLSerialize<>().generateElement(item, 1));
        } else {
            try {
                dl list = new dl();
                PropertyDescriptor[] pda = Introspector.getBeanInfo(item.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor pd : pda) {
                    list.add(new dt(pd.getName()));
                    list.add(new dd().add(String.valueOf(pd.getReadMethod().invoke(item))));
                }
                d.add(list);
            } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        d.add( new a( "Edit " + item.toString()).href( editUrlForItem(item)).rel( "edit"));
        return d;
    }

    public static Object getItem(String itemType, String itemId) {
        Resource<?> resource = EksIndex.resources().stream().filter( r -> r.getName().equalsIgnoreCase( itemType)).findFirst().get();
        return resource.locator().getById(itemId);
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
