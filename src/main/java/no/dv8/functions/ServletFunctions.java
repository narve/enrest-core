package no.dv8.functions;

import no.dv8.eks.rest.EksHTML;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServletFunctions {

    public static <T> BiConsumer<HttpServletRequest, HttpServletResponse> consumer(Function<HttpServletRequest, T> mapper, BiConsumer<T, HttpServletResponse> outputter) {
        return (req, res) -> outputter.accept(mapper.apply(req), res);
    }


    public static <T> XBiConsumer<HttpServletRequest, HttpServletResponse> consumer(Function<HttpServletRequest, T> mapper) {
        return (req, res) -> {
            T entity = mapper.apply(req);
            Element element = entity instanceof Element ? (Element) entity : new XHTMLSerialize().generateElement(entity, 3);
            res.getWriter().print(EksHTML.complete(element, entity.toString()).toString());
        };
    }


}
