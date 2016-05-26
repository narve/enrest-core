package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.utils.Props;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static no.dv8.eks.rest.EksHTML.relToA;

@Slf4j
public class EksResources {

    final List<Resource<?>> resources = new ArrayList<>();
    public ResourcePaths urlCreator;

    public EksResources(String basePath) {
        this.urlCreator = new ResourcePaths(basePath);
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

    public <T> Optional<Resource<?>> locateByRel(String name) {
        return locate(cr -> cr.getName().equals(name), "rel='" + name + "'");
    }

    public <T> Resource<?> locateByName(String itemType) {
        return locate(r -> r.getName().equalsIgnoreCase(itemType), "type='" + itemType + "'").get();
    }

    public <T> Optional<Resource<?>> locateByClz(Class<T> clz) {
        return locate(r -> r.clz().equals(clz), "clz='" + clz.getName() + "'");
    }

    public <T> Optional<Resource<?>> locate(Predicate<Resource> filter, String arg) {
        log.info("Locating resource where {}", arg);
        Optional<Resource<?>> res = resources().stream().filter(filter).findFirst();
        return res;
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

    public List<QueryResource> queries() {
        return resources()
          .stream()
          .map(r -> r.queries())
          .reduce(new ArrayList<>(), (a, b) -> {
              a.addAll(b);
              return a;
          });
    }

    public Collection<?> executeQuery(String name, HttpServletRequest req) {
        log.info("Executing query {}", name);
        String queryName = name.replaceAll("\\-", "\\_");
        log.info("Query name: {}", queryName);
        QueryResource qr = queryForRel( name );
        return qr.query(req);
    }
//
//    public ul toUL( Collection<a> result ) {
//        ul ul = new ul();
//        result.forEach(i -> ul.add(i));
//        return ul;
//    }

}
