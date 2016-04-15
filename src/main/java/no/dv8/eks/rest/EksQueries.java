package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.model.Question;
import no.dv8.eks.model.User;
import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Rels;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.xhtml.generation.elements.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.enrest.mutation.FormHelper.control;

@Slf4j
public class EksQueries {

    static final String pathToQueries = "queries";
    static final String pathToQueryResult = "query-result";
    final String basePath;
    UserResource users = new UserResource();
    QuestionResource questions = new QuestionResource();
    public List<QueryResource> queries = asList(
      new SimpleQuery<User>(Rels.users_search.toString(), s -> users.search(s)),
      new SimpleQuery<Question>(Rels.questions_search.toString(), s -> questions.search(s))
    );

    public EksQueries(String basePath) {
        this.basePath = basePath;
    }

    public ul queriesAsList() {
        ul l = new ul();
        queries
          .stream()
          .map(q -> relToA(q.getRel(), basePath + "/" + pathToQueries + "/"))
          .map(a -> new li().add(a))
          .forEach(i -> l.add(i));
        return l;
    }

    public ul executeQuery(String name, HttpServletRequest req) {
        log.info("Executing query, users={}", users);
        String queryName = name.replaceAll("\\-", "\\_");
        log.info("Query name: {}", queryName);
        QueryResource qr = queries.stream().filter(q -> q.getRel().equals(queryName.replaceAll("_", "-"))).findFirst().get();
        Collection<?> result = qr.query(req);
        ul ul = new ul();
        result.forEach(i -> ul.add(listItem(i)));
        return ul;
    }

    public li listItem(Object u) {
        return new li().add(
          new a(u.toString()).href(new EksResources(basePath).viewUrlForItem(u))
        );
    }


    public form searchForm(Object rel) {
        return new form()
          .clz(rel)
          .get()
          .action(basePath + "/" + pathToQueryResult + "/" + rel)
          .add(
            new fieldset()
              .add(new legend(rel.toString()))
              .add(control(new input().text().name(Names.search).id(Names.search), Names.search))
              .add(new input().submit().value(rel))
          );
    }

}
