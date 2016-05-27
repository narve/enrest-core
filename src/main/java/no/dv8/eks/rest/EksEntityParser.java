package no.dv8.eks.rest;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.utils.Props;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class EksEntityParser implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final EksResources resources;

    public EksEntityParser(EksResources resources) {
        this.resources = resources;
    }


    private <T> T propMapFromJson(Class<T> clz, HttpServletRequest req) throws IOException {
        String s = readBody(req);
        T o = new Gson().fromJson(s, clz);
        log.info("Parsed JSON: {}", o);
        return o;
    }

    private String readBody(HttpServletRequest req) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()))) {
            String s;
            StringBuilder sb = new StringBuilder();
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\r\n");
            }
            log.info("Read body: {}", sb);
            return sb.toString();
        }
    }

    @Override
    public Exchange apply(Exchange exchange) {
        Object q;
        String type = resources.urlCreator.type(exchange.getFullPath());
        Optional<Resource<?>> resourceO = resources.findByName(type);
        if( !resourceO.isPresent() ) {
            return exchange;
        }
        Resource<?> resource = resourceO.get();
        Mutator mutator = resource.creator();
        String contentType = exchange.header("Content-Type");
        if (contentType != null && contentType.contains("json")) {
            try {
                q = propMapFromJson(resource.clz(), exchange.req);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Map<String, String> vals = new Props().single(exchange.req.getParameterMap());
            q = resource.locator().apply(resources.urlCreator.id(exchange.getFullPath())).get();
            q = mutator.setProps(q, vals);
        }
        return exchange.withEntity(q);
    }

    @Override
    public boolean test(Exchange exchange) {
        ResourcePaths urls = resources.urlCreator;
        return urls.isCreateResult(exchange.getFullPath()) || urls.isItem(exchange.getFullPath());
    }
}
