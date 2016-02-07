package no.dv8.rest2.framework;

import lombok.extern.slf4j.Slf4j;
import no.dv8.rest3.Enrest;
import no.dv8.rest3.EnrestResource;
import no.dv8.rest3.Parameter;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.h3;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.ElementDecorator;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

@Slf4j
public abstract class EnrestServletBase extends HttpServlet {

    public Enrest getEnrest() {
        return configure(new Enrest());
    }

    public Enrest configure(Enrest inst) {
        return inst;
    }

    public String getRootPath() {
        return "";
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String ct = req.getHeader("Accept");
        String ct1 = ct.split(",")[0];

        if ("text/html".equals(ct1)) {
            resp.setContentType("text/html");
        } else if ("application/json".equals(ct1)) {
            resp.setContentType("application/json");
        } else {
            throw new IllegalArgumentException("Unknown Accept: " + ct);
        }

//        RestContainer container = getRestContainer();
        String url = req.getRequestURI();

        String ctxPath = req.getServletContext().getContextPath();
        String rootPath = ctxPath + getRootPath();
        String _path = url.substring(rootPath.length() + 1);
        if (_path.startsWith("/"))
            _path = _path.substring(1);

        String path = _path;

        ServletOutputStream outputStream = resp.getOutputStream();

        if (path.isEmpty()) {
            for (Element e : getEnrest().index()) {
                outputStream.print(e.toHTML());
            }
        } else {
            Optional<EnrestResource> first = getEnrest().list().stream().filter(t -> matches(t, path) != null).findFirst();
            if (!first.isPresent()) {
                throw new FileNotFoundException("Don't know how to handle " + path);
            }
            Map<String, String> pparams = matches(first.get(), path);
            req.setAttribute("path-param-map", pparams);

            EnrestResource<?, ?> r = first.get();
            handle(r, req, resp);
        }


        outputStream.close();
    }


    <From, To> void handle(EnrestResource<From, To> r, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        To res;
        if (r.getFrom().equals(Void.class)) {
            res = r.getHandler().apply(null);
        } else {
            From from = r.getReqParser().apply(req);
//                Class<?> from = r.getFrom();
//                for( Parameter p: (List<Parameter>)r.getQueryParams() ) {
//                    p.setValue( req.getParameterMap().get( p.getName() ) == null ? null : req.getParameterMap().get( p.getName() )[0]);
//                }
//                String s = ((List<Parameter>)r.getQueryParams()).get(0).getValue();
            res = r.getHandler().apply(from);
        }

        log.info("Res: {}", res);


        Collection<To> coll;
        if (res instanceof Collection) {
            coll = (Collection<To>) res;
        } else {
            coll = asList(res);
        }

        Map<To, List<Link>> links = new HashMap<>();
        coll.forEach(to -> links.put(to, getEnrest().getLinker().linksFrom(r, to)));

        String html = html(res, links);
        resp.getOutputStream().print(html);
    }

    Map<String, String> matches(EnrestResource t, String path) {
        if (t.getPath().equals(path)) return new HashMap<>();

        if (t.getPathParams().isEmpty())
            return null;

        String re = Pattern.quote(t.getPath());
        for (Parameter p : (List<Parameter>) t.getPathParams()) {
            re += "/([^/]+)";
        }

        Matcher matcher = Pattern.compile(re).matcher(path);
        if (matcher.matches()) {
            Map<String, String> ppMap = new HashMap<>();
            for (int i = 0; i < t.getPathParams().size(); i++) {
                ppMap.put(((Parameter) t.getPathParams().get(i)).getName(), matcher.group(i + 1));
            }
            return ppMap;
        }

        return null;
    }

    Object extractParams(HttpServletRequest req, Transition t) {
        return null;
    }
//
//    RestContainer getRestContainer() {
//        return RestSetup.setup();
//    }


    <To> String html(Object obj, Map<To, List<Link>> linkMap) {
        XHTMLSerialize<?> ser = new XHTMLSerialize<>();
//        ser.typeMap.put(Transition.class, new XHTMLSerializer<Transition>() {
//            @Override
//            public Element<?> generateElement(Transition transition, int i) {
//                return new a(transition.toString()).href(transition.url);
//            }
//        });

//        ser.typeMap.put(Concert.class, new XHTMLSerializer<Concert>() {
//            @Override
//            public Element<?> generateElement(Concert obj, int i) {
//                return new a(obj.toString()).href(obj.url);
//            }
//        });
        ElementDecorator dec = new ElementDecorator() {
            @Override
            public <T extends Element> Element<T> decorate(Element<T> element, Object o, int level) {
                element.addClz(o.getClass().getSimpleName());
                element.addClz("level" + level);

                if (level != 1)
                    return element;

//                RestContainer container = getRestContainer();
//                List<Link> links = getEnrest().getLinker().linksFrom(null, o);
                List<Link> links = linkMap.get(o);
                if (!links.isEmpty()) {
                    element.add(new h3("Links/transitions"));
                    ul ul = new ul().clz("transitions");
                    for (Link t : links) {
                        ul.add(
                          new li()
                            .add(
                              new a(t.toString())
                                .rel(t.getRel())
                                .href(t.getTarget() != null ? t.getTarget().getPath() : "unknown target")
                            ));
                    }
                    element.add(ul);
                }
                return element;
            }
        };
        ser.decorator = dec;
//        if (true) {
//            Element<?> element1 = ser.generateElement(asList(new Transition<>()), 1);
//            return element1.toHTML();
//        }
        Element<?> element = ser.generateElement(obj, 2);
        return Enrest.full(asList(element)).get(0).toHTML();
    }

//    private String json(List<Transition> roots) {
//        JsonSerializer<Class> x = new JsonSerializer<Class>() {
//            @Override
//            public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
//                return new Gson().toJsonTree(src.getName());
//            }
//        };
//
//        Gson gson = new GsonBuilder()
//          .registerTypeAdapter(Class.class, x)
//          .create();
//
//        JsonArray ar = new JsonArray();
//        roots.stream().map(s -> gson.toJsonTree(s)).forEach(ar::add);
//
//        return gson.toJson(ar);
//
//    }
}
