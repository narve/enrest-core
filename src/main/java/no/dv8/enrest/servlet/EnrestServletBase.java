package no.dv8.enrest.servlet;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.EnrestResource;
import no.dv8.enrest.container.Enrest;
import no.dv8.enrest.model.Link;
import no.dv8.enrest.model.Parameter;
import no.dv8.enrest.model.Transition;
import no.dv8.enrest.resources.Resource;
import no.dv8.enrest.spi.Outputter;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.div;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;
import no.dv8.xhtml.serializer.XHTMLSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

@Slf4j
public abstract class EnrestServletBase extends HttpServlet {
//
//    public abstract Enrest getEnrest();
////
////    public Enrest configure(Enrest inst) {
////        return inst;
////    }
//
//    public String getRootPath() {
//        return "";
//    }
//
//    @Override
//    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Resource<?> res = findResource( req );
//        if( res == null ) {
//            throw new NullPointerException( String.format( "Unable to locate resource for %s", req.getRequestURL() ) );
//        }
//        String ct = getContentType(req);
//        Object result = new Object(); //handle(res, req, resp);
//        Outputter outputter = getOutputter(ct);
//        outputter.output(result, resp);
//    }
//
//    <To> Resource<To> findResource(HttpServletRequest req) {
//
//
//
//        String url = req.getRequestURI();
//
//        String ctxPath = req.getServletContext().getContextPath();
//        String rootPath = ctxPath + getRootPath();
//        String _path = url.substring(rootPath.length()-1);
////        if (_path.startsWith("/"))
////            _path = _path.substring(1);
//
//        String path = _path;
//
//        if (path.equals("/")) {
//            return (Resource<To>) getEnrest().indexResource();
//
//        } else if (path.startsWith("/_resource")) {
////            outputStream.print( "resource: " + path );
//            String ref = path.substring("/_resource/".length());
//            Resource r = getEnrest().getResources().stream().filter(res -> res.getName().equals(ref)).findFirst().get();
//            return r;
//        } else {
//            String xp = path.startsWith("/") ? path.substring(1) : path;
//            Optional<Resource> first = getEnrest().list().stream().peek(r -> log.info("Res-name: " + r.getName())).filter(t -> true ).findFirst();
//            if (!first.isPresent()) {
//                throw new IllegalStateException("Don't know how to locate " + xp);
//            }
//            Map<String, String> pparams = new HashMap<>(); //matches(first.get(), path);
//            req.setAttribute("path-param-map", pparams);
//
//            Resource<To> r = first.get();
//            return r;
//        }
//    }
//
//    private String getContentType(HttpServletRequest req) {
//        String ct = req.getHeader("Accept");
//        String ct1 = ct.split(",")[0];
//        return ct1;
//    }
//
//    private Outputter getOutputter(String ct) {
//        return htmlOutputter();
//    }
//
//    private Outputter htmlOutputter() {
//        return (o, s) -> {
//            s.setContentType("text/html");
//            if( (o instanceof List) && ((List)o).size() == 1 && ((List)o).get(0) instanceof Element ){
//                s.getWriter().write( ((Element) ((List)o).get(0)).toHTML() );
//            } else {
//                XHTMLSerializer ser = new XHTMLSerialize<>();
//                Element element = ser.generateElement(o, 1);
//                s.getOutputStream().print(element.toString());
//            }
////            if ("text/html".equals(ct1)) {
////            } else if ("application/json".equals(ct1)) {
////                resp.setContentType("application/json");
////            } else {
////                throw new IllegalArgumentException("Unknown Accept: " + ct);
////            }
//
//        };
//    }
//
//
//
////    <From, To> List<To> handle(Resource<To> r, HttpServletRequest req, HttpServletResponse resp) throws IOException {
////        List<To> res;
////        if (r.getFrom().equals(Void.class)) {
////            res = r.getHandler().apply(null);
////        } else {
////            From from = r.getReqParser().apply(req);
////            res = r.getHandler().apply(from);
////        }
////
////        log.info("Res: {}", res);
////        return res;
////    }
//
//    <From, To> String html(EnrestResource<From, To> resource, List<To> obj) {
//        XHTMLSerialize<?> ser = new XHTMLSerialize<>();
////        ser.typeMap.put(Transition.class, new XHTMLSerializer<Transition>() {
////            @Override
////            public Element<?> generateElement(Transition transition, int i) {
////                return new a(transition.toString()).href(transition.url);
////            }
////        });
//
////        ser.typeMap.put(Concert.class, new XHTMLSerializer<Concert>() {
////            @Override
////            public Element<?> generateElement(Concert obj, int i) {
////                return new a(obj.toString()).href(obj.url);
////            }
////        });
////        ElementDecorator dec = new ElementDecorator() {
////            @Override
////            public <T extends Element> Element<T> decorate(Element<T> element, Object o, int level) {
////                element.addClz(o.getClass().getSimpleName());
////                element.addClz("level" + level);
////
////                if (level != 1)
////                    return element;
////
//////                RestContainer container = getRestContainer();
//////                List<Link> links = getEnrest().getLinker().linksFrom(null, o);
////                List<Link> links = linkMap.get(o);
////                if (!links.isEmpty()) {
////                    element.insert(new h3("Links/transitions"));
////                    ul ul = new ul().clz("transitions");
////                    for (Link t : links) {
////                        ul.insert(
////                          new li()
////                            .insert(
////                              new a(t.toString())
////                                .rel(t.getRel())
////                                .href(t.getTarget() != null ? t.getTarget().getPath() : "unknown target")
////                            ));
////                    }
////                    element.insert(ul);
////                }
////                return element;
////            }
////        };
////        ser.decorator = dec;
////        if (true) {
////            Element<?> element1 = ser.generateElement(asList(new Transition<>()), 1);
////            return element1.toHTML();
////        }
//
//        div container = new div();
//
//        for (To to : obj) {
//            container.add(ser.generateElement(to, 2));
//            container.add(html(resource.getLinker().apply(to)));
//        }
////        obj.forEach( to -> container.insert( html(to)));
////
////        Element<?> element = ser.generateElement(obj, 2);
////        List<Element<?>> elements = new ArrayList<>();
////        elements.insert(element);
////
////        elements.insert(html(links));
////
////
//        return Enrest.full(asList(container)).get(0).toHTML();
//    }
//
//    private Element<?> html(List<Link> links) {
//
//        ul ul = new ul().clz("transitions");
//        for (Link t : links) {
//            ul.add(
//              new li()
//                .add(
//                  new a(t.toString())
//                    .rel(t.getRel())
//                    .href(t.getTarget() != null ? t.getTarget().getPath() : "unknown target")
//                ));
//        }
//
//        return ul;
//    }
//
////    private String json(List<Transition> roots) {
////        JsonSerializer<Class> x = new JsonSerializer<Class>() {
////            @Override
////            public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
////                return new Gson().toJsonTree(src.getRel());
////            }
////        };
////
////        Gson gson = new GsonBuilder()
////          .registerTypeAdapter(Class.class, x)
////          .create();
////
////        JsonArray ar = new JsonArray();
////        roots.stream().map(s -> gson.toJsonTree(s)).forEach(ar::insert);
////
////        return gson.toJson(ar);
////
////    }
}
