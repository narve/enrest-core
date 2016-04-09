package no.dv8.eks.rest;

import no.dv8.eks.controllers.Controller;
import no.dv8.eks.controllers.Questions;
import no.dv8.eks.controllers.Users;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksServlet.basePath;

public class EksResources {

    List<Controller<?>> controllers = asList( Users.instance(), Questions.instance() );

    public static final String pathToResource = "view-resource";
    public static final String editPathToResource = "edit-resource";

    public String viewUrlForItem(Object o ) {
        return basePath + pathToResource + "/" + itemClass( o ) + "/" + itemId( o );
    }

    public String editUrlForItem(Object o ) {
        return basePath + editPathToResource + "/" + itemClass( o ) + "/" + itemId( o );
    }

    String itemId(Object o) {
        try {
            return o.getClass().getMethod( "getId").invoke( o).toString();
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    String itemClass(Object o) {
        return o.getClass().getSimpleName().toLowerCase();
    }

    public Element<?> itemToElement(String substring) {
        return toElement( getItem( substring));
    }

    public Object getItem(String substring) {
        String itemClass = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        return getItem(itemClass, itemId);
    }

    Element<?> toElement(Object item) {
        div d = new div();
        try {
            dl list = new dl();
            PropertyDescriptor[] pda = Introspector.getBeanInfo(item.getClass()).getPropertyDescriptors();
            for( PropertyDescriptor pd: pda ) {
                list.add( new dt(pd.getName() ));
                list.add( new dd().add( String.valueOf( pd.getReadMethod().invoke(item))));
            }
            d.add(list);
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

        d.add( new a( "Edit " + item.toString()).href( editUrlForItem(item)).rel( "edit"));

        return d;
    }

    public Object getItem(String itemType, String itemId) {
        Controller<?> x = controllers.stream().filter( c -> c.getClz().getSimpleName().equalsIgnoreCase(itemType)).findFirst().get();
        return x.getById( itemId );
    }

    public Element<?> executeUpdate(String substring, HttpServletRequest req) {
        String itemClass = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        Object item = getItem(substring);
        CreatorResource creatorResource = new EksForms().locateByClz(itemClass);
        Object q = creatorResource.setProps(item, req);
        creatorResource.update(q);
        return toElement(q);
    }
}
