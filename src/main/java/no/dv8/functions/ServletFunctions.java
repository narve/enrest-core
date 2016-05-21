package no.dv8.functions;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.rest.EksHTML;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.*;

@Slf4j
public class ServletFunctions {

    public static <T> BiConsumer<HttpServletRequest, HttpServletResponse> consumer(Function<HttpServletRequest, T> mapper, BiConsumer<T, HttpServletResponse> outputter) {
        return (req, res) -> outputter.accept(mapper.apply(req), res);
    }

    public static <T> Predicate<T> exMeansFalse(Predicate in) {
        return t -> {
            try {
                return in.test(t);
            } catch (Exception e) {
                log.warn("Predicate evaluation failed: {}", e);
                return false;
            }
        };
    }



    public static <T> UnaryOperator<T> returner(Consumer<T> consumer) {
        return x -> {
            consumer.accept(x);
            return x;
        };
    }

    public static <T> XBiConsumer<HttpServletRequest, HttpServletResponse> consumer(Function<HttpServletRequest, T> mapper) {
        return (req, res) -> {
            T entity = mapper.apply(req);
            Element element = entity instanceof Element ? (Element) entity : new XHTMLSerialize().generateElement(entity, 3);
            res.getWriter().print(EksHTML.complete(element, entity.toString()).toString());
        };
    }

    public static Predicate<HttpServletRequest> startsWith(String prefix) {
        return new Predicate<HttpServletRequest>() {
            @Override
            public boolean test(HttpServletRequest req) {
                return req.getPathInfo() != null && req.getPathInfo().startsWith(prefix);
            }

            @Override
            public String toString() {
                return "startsWith " + prefix;
            }
        };
    }


}
