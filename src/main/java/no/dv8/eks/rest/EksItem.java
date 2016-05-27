package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Resource;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class EksItem implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final EksResources resources;

    public EksItem(EksResources resources) {
        this.resources = resources;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isItem(x.getFullPath());
    }

    @Override
    public Exchange apply(Exchange exchange) {
        String itemClass = resources.urlCreator.type(exchange.getFullPath());
        String itemId = resources.urlCreator.id(exchange.getFullPath());
        Resource<?> resource = resources.getByName(itemClass);
        Optional<?> item = resource.locator().apply(itemId);
        if (!item.isPresent()) {
            throw new IllegalArgumentException("Not found: " + itemClass + "#" + itemId);
        }

        switch (exchange.req.getMethod().toUpperCase()) {
            case "GET":
//                return exchange.withEntity(resources.toElement(item.get()));
                return exchange.withEntity(item.get());
            case "POST":
            case "PUT":
//                return exchange.withEntity(executeUpdate(resource, item.get(), exchange.req));
                return exchange.withEntity(resource.updater().update(exchange.getEntity()));

            default:
                throw new UnsupportedOperationException(exchange.toString());
        }
    }
//
//    public Object executeUpdate(Resource<?> resource, Object item, HttpServletRequest req) {
//
//        Mutator mutator = resource.creator();
//        String contentType = req.getHeader("Content-Type");
//        Map<String, String> vals;
//        Object q;
//        if( contentType.contains( "json" ) ) {
//            try {
//                q = propMapFromJson( resource.clz(), req );
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        } else {
//            vals = new Props().single(req.getParameterMap());
//            q = mutator.setProps(item, vals);
//        }
//
//        q = mutator.update(q);
////        return toElement(q);
//        return q;
//    }
//
//    private <T> T propMapFromJson(Class<T> clz, HttpServletRequest req) throws IOException {
//        String s = readBody(req);
////        Map m = new Gson().fromJson( s, HashMap.class );
//        T o = new Gson().fromJson(s, clz);
//        log.info( "Parsed JSON: {}", o );
//
//        return o;
//    }
//
//    private String readBody( HttpServletRequest req ) throws IOException {
//        try( BufferedReader br = new BufferedReader( new InputStreamReader( req.getInputStream() ) ) ) {
//            String s;
//            StringBuilder sb = new StringBuilder();
//            while( (s=br.readLine() ) != null) {
//                sb.append( s ).append( "\r\n" );
//            }
//            log.info( "Read body: {}", sb );
//            return sb.toString();
//        }
//    }

}
