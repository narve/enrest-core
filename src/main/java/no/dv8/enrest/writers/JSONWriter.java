package no.dv8.enrest.writers;

import com.google.gson.*;
import no.dv8.enrest.Exchange;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.generation.support.ElementBase;
import no.dv8.xhtml.generation.support.Str;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static no.dv8.utils.Maps.mapOf;

public class JSONWriter implements UnaryOperator<Exchange> {
    static <T> T single(List<T> list) {
        if (list.size() == 1)
            return list.get(0);
        else throw new IllegalArgumentException("Can't get single item from a list of " + list.size() + " items. ");
    }

    @Override
    public Exchange apply(Exchange exchange) {
        exchange =
          exchange.withContentType("application/json")
            .withCharacterEncoding("utf-8");

        if (exchange.getOutEntity() instanceof ElementBase) {
            exchange = exchange.withOutEntity(transformToMap(exchange.getOutEntity()));
        }
        try {
            Gson gson = gson();
//            JsonObject linkObj = new JsonObject();
//            exchange.getLinks().forEach( linkObj.add( elementSerializer().serialize()))
            JsonArray links = gson.toJsonTree(exchange.getLinks()).getAsJsonArray();
            PrintWriter writer = exchange.getWriter();
            JsonElement s = gson.toJsonTree(exchange.getOutEntity());
            if (s.isJsonObject()) {
                ((JsonObject) s).add("_links", links);
            }
            writer.write(s.toString());
            writer.close();
            return exchange;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Object transformToMap(Element<?> x) {
//        if( x instanceof ul) {
//            return x.getChildren().stream().map( this::transformToMap).collect( toList() );
//        } else if( x instanceof li) {
//            return transformToMap(single(x.getChildren()));
//        } else {

        if (x instanceof Str) {
//            return ((Str)x).text.toString().trim(); // TODO
            return x.toString().trim();
        } else if (x.getAttributes().isEmpty() && x.getChildren().size()==1) {
            return transformToMap(single(x.getChildren()));
        } else if (x.getAttributes().isEmpty() && !x.getChildren().isEmpty()) {
            return x.getChildren().stream().map(this::transformToMap).collect(toList());
        }

        List<Element> nonStrChildren = x.getChildren().stream().filter( c -> !(c instanceof Str )).collect(toList());
        List<Element> strChildren = x.getChildren().stream().filter( c -> c instanceof Str ).collect(toList());


        Map<String, Object> m = mapOf(
          "_type", x.name()
        );

        StringBuilder content = new StringBuilder();
        x.getChildren().stream().filter( c -> c instanceof Str ).forEach( content::append );
        if( content.length() > 0 ) {
            m.put( "_text", strChildren.stream().map( Object::toString ).collect(Collectors.joining() ));
        }
        x.getAttributes().entrySet().stream()
          .forEach(me -> m.put(me.getKey().toString(), me.getValue()));

        if (!nonStrChildren.isEmpty()) {
            m.put("items", x.getChildren().stream().map(this::transformToMap).collect(toList()));
        }
//        return mapOf(
//          "elementName", x.name(),
//          "children", x.getChildren().stream().map( this::transformToMap).collect( toList() )
//        );
        return m;
//            return gson().toJsonTree(x);
//        }
//        return mapOf( "here", "content" );
    }

    private Gson gson() {
        return new GsonBuilder()
          .setPrettyPrinting()
//          .registerTypeAdapter( Element.class, elementSerializer())
          .create();
    }
//
//    private JsonSerializer<Element<?>> elementSerializer() {
//        return new JsonSerializer<Element<?>>() {
//            @Override
//            public JsonElement serialize(Element<?> src, Type typeOfSrc, JsonSerializationContext context) {
//                JsonObject element = new JsonObject();
//                element.addProperty( "_element", src.name());
//                src.getAttributes().forEach( (o1,o2) -> element.addProperty( o1.toString(), o2.toString() ) );
//                return element;
//            }
//        };
//    }
}
