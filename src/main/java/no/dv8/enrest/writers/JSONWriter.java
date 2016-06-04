package no.dv8.enrest.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import no.dv8.enrest.Exchange;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.UnaryOperator;

public class JSONWriter implements UnaryOperator<Exchange> {
    @Override
    public Exchange apply(Exchange exchange) {
//        Element<?> result = (Element<?>) (exchange.getEntity());
        String title = exchange.getPathInfo();

        exchange =
          exchange.withContentType("application/json")
            .withCharacterEncoding("utf-8");
        try {
            Gson gson = gson();
//            JsonObject linkObj = new JsonObject();
//            exchange.getLinks().forEach( linkObj.add( elementSerializer().serialize()))
            JsonArray links = gson.toJsonTree(exchange.getLinks()).getAsJsonArray();
            PrintWriter writer = exchange.getWriter();
            JsonObject s = gson.toJsonTree(exchange.getEntity()).getAsJsonObject();
            s.add("_links", links);
            writer.write(s.toString());
            writer.close();
            return exchange;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
