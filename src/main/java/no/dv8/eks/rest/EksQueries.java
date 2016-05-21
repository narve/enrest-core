package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.enrest.resources.FormHelper.control;

@Slf4j
public class EksQueries {

    final EksResources resources;

    public EksQueries(EksResources resources) {
        this.resources = resources;
    }

    UserResource users = new UserResource();
    public List<QueryResource> queries() {
        return resources.resources()
          .stream()
          .map( r -> r.queries() )
          .reduce( new ArrayList<>(), (a,b) -> { a.addAll(b); return a; });
    }

    public ul queriesAsList() {
        ul l = new ul();
        queries()
          .stream()
          .map(q -> relToA(q.getRel(), resources.urlCreator.query(q.getRel())))
          .map(a -> new li().add(a))
          .forEach(i -> l.add(i));
        return l;
    }

    public ul executeQuery(String name, HttpServletRequest req) {
        log.info("Executing query {}", name );
        String queryName = name.replaceAll("\\-", "\\_");
        log.info("Query name: {}", queryName);
        Optional<QueryResource> qr = queries().stream().filter(q -> q.getRel().equals(queryName.replaceAll("_", "-"))).findFirst();
        if( !qr.isPresent() ) {
            throw new UnsupportedOperationException("No such query: '" + queryName + "'");
        }
        Collection<?> result = qr.get().query(req);
        ul ul = new ul();
        result.forEach(i -> ul.add(listItem(i)));
        return ul;
    }

    public li listItem(Object u) {
        return new li().add(
          new a(u.toString()).href(resources.urlCreator.viewItem(resources.itemClass(u), resources.itemId(u)))
        );
    }

    public form searchForm(Object rel) {
        QueryResource q = resources.queryForRel(rel);
        List<Element<?>> controls = q.params()
          .stream()
          .map( p -> control( new input().type(p.getHtmlType()).name(p.getName()).id(p.getName()), p.getName()) )
          .collect( toList() );
        return new form()
          .clz(rel)
          .get()
          .action(resources.urlCreator.queryResult(rel))
          .add(
            new fieldset()
              .add(new legend(rel.toString()))
              .add( controls )
              .add(new input().submit().value(rel))
          );
    }

}
