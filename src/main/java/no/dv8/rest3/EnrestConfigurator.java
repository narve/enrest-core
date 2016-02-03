package no.dv8.rest3;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import no.dv8.concerts.Concert;
import no.dv8.concerts.Program;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

@Slf4j
public class EnrestConfigurator {

    public static Enrest getEnrest() {
        Program p = new Program();
        Enrest r = new Enrest();
        r.single(String.class, Concert.class)
          .method("GET")
          .queryParam("id")
          .name("Get concert by id - Query")
          .reqParser(req -> req.getParameterMap().get("id")[0])
          .handler(p::getConcert)
          .buildAndRegister();

        r.single(String.class, Concert.class)
          .method("GET")
          .pathParam("id")
          .name("Get concert by id - Path")
          .reqParser(req -> ((Map<String, String>) req.getAttribute("path-param-map")).get("id"))
          .handler(p::getConcert)
          .buildAndRegister();

        r.collection(Void.class, Concert.class)
          .method("GET")
          .name("List concerts")
          .handler((x) -> p.getConcerts())
          .buildAndRegister();

        r.single(Concert.class, Concert.class)
          .method("POST")
          .name("Insert concert")
          .reqParser(req -> parseJSON(Concert.class, getBodyAsString(req)))
          .jsonBodyParam()
          .handler(x -> p.addConcert(x))
          .buildAndRegister();

        return r;
    }


    static <T> T parseJSON(Class<T> clz, String bodyAsString) {

        try {
            bodyAsString = URLDecoder.decode(bodyAsString, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        log.info( "Parsing: {}", bodyAsString );
        return new Gson().fromJson(bodyAsString, clz);
    }

    static String getBodyAsString(ServletRequest req) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
            String s;
            StringBuilder sb = new StringBuilder();
            while ((s = br.readLine()) != null) sb.append(s).append("\r\n");
            return sb.toString().substring( "body=".length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
