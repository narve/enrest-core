package no.dv8.enrest.handlers;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourcePaths;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.utils.Props;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class EntityParser implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final ResourceRegistry resources;

    public EntityParser(ResourceRegistry resources) {
        this.resources = resources;
    }


    private <T> T propMapFromJson(Class<T> clz, InputStream req) throws IOException {
        String s = readBody(req);
        T o = new Gson().fromJson(s, clz);
        log.info("Parsed JSON: {}", o);
        return o;
    }

    private String readBody(InputStream req) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(req))) {
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
        String type = resources.getPaths().type(exchange.getFullPath());
        Optional<Resource<?>> resourceO = resources.findByName(type);
        if( !resourceO.isPresent() ) {
            return exchange;
        }
        Resource<?> resource = resourceO.get();
        Mutator mutator = resource.creator();
        String contentType = exchange.getHeader("Content-Type");
        if (contentType != null && contentType.contains("json")) {
            try {
                q = propMapFromJson(resource.clz(), exchange.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Map<String, String> vals = new Props().single(exchange.getParameterMap());
            if( resources.getPaths().isCreateResult(exchange.getFullPath())) {
                try {
                    q = resource.clz().newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String id = resources.getPaths().id(exchange.getFullPath());
                q = resource
                  .locator()
                  .apply(id)
                  .orElseThrow( () -> new RuntimeException( "Unable to find " + type + " with id " + id ) );
            }
            q = mutator.setProps(q, vals);
        }
        return exchange.withInEntity(q);
    }

    @Override
    public boolean test(Exchange exchange) {
        ResourcePaths urls = resources.getPaths();
        return urls.isCreateResult(exchange.getFullPath()) || urls.isItem(exchange.getFullPath());
    }
}
