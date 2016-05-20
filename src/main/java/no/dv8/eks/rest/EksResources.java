package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.reflect.Props;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class EksResources {

    final String basePath;
    final List<Resource<?>> resources = new ArrayList<>();
    ResourcePaths urlCreator = new ResourcePaths();

    public EksResources(String basePath) {
        this.basePath = basePath;
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
        return basePath + "/" + urlCreator.viewItem(itemClass(o), itemId(o));
    }

    public String editUrlForItem(Object o) {
        return basePath + "/" + urlCreator.editItem(itemClass(o), itemId(o));
    }

    public <T> Element<?> toElement(T item) {
        Resource<T> r = (Resource<T>) locateByClz(item.getClass()).get();

        List<a> links = r.linker().links(item);

        for (a link : links) {
            Optional<Resource<?>> sub = locateByClz(link.href().getClass());
            if (sub.isPresent()) {
                log.info("Converting link using {}", sub);
                switch (link.getAttributes().getOrDefault("rel", "").toString()) {
                    case "edit":
                        link.href(editUrlForItem(link.href()));
                        break;
                    case "self":
                    default:
                        link.href(viewUrlForItem(link.href()));
                        break;
                }
            }
        }

        div d = new div();
        d.add(new XHTMLSerialize<>().generateElement(item, 1));
        d.add(new div().clz("links").add(
          links.stream().map(l -> new li().add(l)).collect(toList())
        ));
        return d;
    }

    public Element<?> executeUpdate(Resource<?> resource, Object item, HttpServletRequest req) {
        Mutator mutator = resource.creator();
        Map<String, String> vals = new Props().single(req.getParameterMap());
        Object q = mutator.setProps(item, vals);
        mutator.update(q);
        return toElement(q);
    }

    public <T> Optional<Resource<?>> locateByRel(String name) {
        return locate(cr -> cr.getName().equals(name), "rel='" + name + "'");
    }

    public <T> Optional<Resource<?>> locateByName(String itemType) {
        return locate(r -> r.getName().equalsIgnoreCase(itemType), "type='" + itemType + "'");
    }

    public <T> Optional<Resource<?>> locateByClz(Class<T> clz) {
        return locate(r -> r.clz().equals(clz), "clz='" + clz.getName() + "'");
    }

    public <T> Optional<Resource<?>> locate(Predicate<Resource> filter, String arg) {
        log.info("Locating resource where {}", arg);
        Optional<Resource<?>> res = resources().stream().filter(filter).findFirst();
        return res;
    }

    public String queryResultUrl(String rel) {
        return basePath + "/" + urlCreator.queryResult(rel);
    }

    public QueryResource queryForRel(Object rel) {
        Optional<QueryResource> first = resources().stream()
          .flatMap(r -> Stream.of(r.queries().toArray(new QueryResource[0])))
          .filter(q -> q.getRel().equals(rel.toString()))
          .findFirst();
        if (!first.isPresent()) {
            throw new IllegalArgumentException("No query '" + rel + "'");
        }
        return first.get();
    }
}
