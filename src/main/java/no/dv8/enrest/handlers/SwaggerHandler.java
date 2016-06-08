package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.queries.QueryResource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;

public class SwaggerHandler implements UnaryOperator<Exchange> {

    final ResourceRegistry resources;

    public SwaggerHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    @Override
    public Exchange apply(Exchange exchange) {
        return exchange.withContentType("application/json").withOutEntity(swagger());
    }

    Map<String, Object> swagger() {
        return mapOf(
          "swagger", "2.0",
          "info", infoObject(),
          "host", "localhost:8080",
          "produces", "text/html",
          "basePath", "/eks/",
          "paths", paths()
        );
    }

    private Map<String, Object> paths() {
        Map<String, Object> paths = new HashMap<>();
        resources.resources().stream()
          .flatMap(r -> r.queries().stream())
          .forEach(q -> paths.put("query-result/" + q.getRel(), queryResponse(q)));

        resources.resources().stream()
          .map( r -> String.join("/", r.getName(), "{id}"))
          .forEach(q -> paths.put(q, itemResponse(null)));
        return paths;
//        return mapOf(
//          "queries/ArticleCollection", mapOf(
//            "get", articleResponses()
//          ),
//          "query-result/ArticleCollection?search={:search}", mapOf(
//            "get", articleResponses()
//          )
//        );
    }

    private Map<String, Object> queryResponse(QueryResource q) {
        return mapOf(
          "get", opResponses()
        );
    }

    private Map<String, Object> itemResponse(QueryResource q) {
        return mapOf(
          "parameters", asList(
            mapOf(
              "name", "id",
              "in", "path",
              "type", "string",
              "items", mapOf(
                "type", "string"
              )
            )
          ),
          "get", opResponses()
        );
    }

    private Map<String, Object> opResponses() {
        return mapOf(
          "200", mapOf(
            "description", "200 ok?"
          ),
          "default", mapOf(
            "description", "default ok?"
          )
        );
    }

    private Map<String, Object> infoObject() {
        return mapOf(
          "title", "the title",
          "description", "the description comes here",
          "version", "0.0.1-SNAPSHOT"
        );
    }

    private Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put(pairs[i].toString(), pairs[i + 1]);
        }
        return m;
    }
}
