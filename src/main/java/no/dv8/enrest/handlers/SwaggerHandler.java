package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.utils.Maps;

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
        return Maps.mapOf(
          "swagger", "2.0",
          "info", infoObject(),
          "host", "localhost:8080",
          "produces", asList( "text/html", "application/json" ),
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
        return Maps.mapOf(
          "get", opResponses()
        );
    }

    private Map<String, Object> itemResponse(QueryResource q) {
        return Maps.mapOf(
          "parameters", asList(
            Maps.mapOf(
              "name", "id",
              "in", "path",
              "type", "string",
              "items", Maps.mapOf(
                "type", "string"
              )
            )
          ),
          "get", opResponses()
        );
    }

    private Map<String, Object> opResponses() {
        return Maps.mapOf(
          "200", Maps.mapOf(
            "description", "200 ok?"
          ),
          "default", Maps.mapOf(
            "description", "default ok?"
          )
        );
    }

    private Map<String, Object> infoObject() {
        return Maps.mapOf(
          "title", "the title",
          "description", "the description comes here",
          "version", "0.0.1-SNAPSHOT"
        );
    }

}
