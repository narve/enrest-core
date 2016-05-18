package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Slf4j
public class EksResources {

    public static final String pathToResource = "view-resource";
    public static final String editPathToResource = "edit-resource";
    final String basePath;
    final List<Resource<?>> resources;

    public EksResources(String basePath, List<Resource<?>> resources) {
        this.basePath = basePath;
        this.resources = resources;
    }

    public static String itemId(Object o) {
        try {
            Object id = o.getClass().getMethod("getId").invoke(o);
            return id != null ? id.toString() : null;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String itemClass(Object o) {
        return o.getClass().getSimpleName().toLowerCase();
    }

    public List<Resource<?>> resources() {
        return resources;
    }

    public String viewUrlForItem(Object o) {
        return basePath + "/" + pathToResource + "/" + itemClass(o) + "/" + itemId(o);
    }

    public String editUrlForItem(Object o) {
        return basePath + "/" + editPathToResource + "/" + itemClass(o) + "/" + itemId(o);
    }

    public <T> Element<?> toElement(T item) {
        Resource<T> r = (Resource<T>) locateByClz(item.getClass()).get();

        List<a> links = r.linker().links(item).stream().map(this::linkToElement).collect(toList());

        for( a link: links ) {
            Optional<Resource<?>> sub = locateByClz(link.href().getClass());
            if( sub.isPresent() ) {
                log.info( "Converting link using {}", sub );
                link.href( viewUrlForItem( link.href()));
            }
        }


        div d = new div();
        d.add(new XHTMLSerialize<>().generateElement(item, 1));
        d.add(new div().clz("links").add(
          links
        ));
        return d;
    }

    public a linkToElement(a l) {
        return l;
//        return new a().href(String.valueOf(l.getTarget())).rel( "therel").add( l.toString() );
    }

    public Element<?> executeUpdate(String substring, HttpServletRequest req) {
        String itemClass = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        Resource<?> resource = locateByName(itemClass).get();
        Object item = resource.locator().apply(itemId);

//        Mutator resourceMutatorResource = eksForms().locateByClz(itemClass);
        Mutator resourceMutatorResource = resource.creator();
        Object q = resourceMutatorResource.setProps(item, req);
        resourceMutatorResource.update(q);
        return toElement(q);
    }


    public <T> Optional<Resource<?>> locateByRel(String name) {
        return locate(cr -> cr.getName().equals(name), "rel='" + name + "'");
    }

    public <T> Optional<Resource<?>> locateByName(String itemType) {
        return locate( r -> r.getName().equalsIgnoreCase(itemType), "type='" + itemType + "'" );
    }

    public <T> Optional<Resource<?>> locateByClz(Class<T> clz) {
        return locate( r -> r.clz().equals(clz), "clz='" + clz.getName() + "'" );
    }

    public <T> Optional<Resource<?>> locate(Predicate<Resource> filter, String arg ) {
        log.info("Locating resource where {}", arg);
        Optional<Resource<?>> res = resources().stream().filter(filter).findFirst();
        return res;
    }

}
