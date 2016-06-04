package no.dv8.enrest.handlers;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.ResourceRegistry;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.utils.Props;
import no.dv8.xhtml.generation.elements.form;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Custom;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static no.dv8.eks.rest.EksHTML.relToA;

@Slf4j
public class CreateFormHandler implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final ResourceRegistry resources;

    public CreateFormHandler(ResourceRegistry resources) {
        this.resources = resources;
    }

    public static input text(Object r) {
        return new input().text().id(r).name(r).placeholder(r.toString());
    }

    public Object executeCreate(Resource<?> resource, HttpServletRequest req) {
        Mutator cr = resource.creator();
        Object createResult;
        try {
            Map<String, String> vals = new Props().single(req.getParameterMap());
            createResult = cr.setProps(resource.clz().newInstance(), vals);
            createResult = cr.create(createResult);
            return createResult;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
//        Element e = resources.toElement(createResult);
//        return new div()
//          .add(new h1("The object:"))
//          .add(e);
    }

    public ul creatorForms() {
        ul list = new ul();
        resources.resources()
          .stream()
          .map(cr -> relToA(cr.getName(), resources.getPaths().createForm(cr.getName())))
          .map(a -> new li().add(a))
          .forEach(list::add);
        return list;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.getPaths().isCreateForm((x.getFullPath()));
    }

    @Override
    public Exchange apply(Exchange x) {
        try {
            String path = x.getFullPath();
            String type = resources.getPaths().type(path);
            Resource res = resources.locateByRel(type).get();
            Object obj = res.clz().newInstance();
            return x.withEntity(getForm(type, "create", res.creator().inputs(obj), "post"));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public form getForm(Object name, String clz, List<Element<?>> inputs, Object method) {
        return new form()
          .addClz( name )
          .addClz( clz )
          .method(method)
          .action(resources.getPaths().createAction(name))
          .set("accept-charset", "utf-8")
          .set("enc-type", "application/x-www-form-urlencoded")
          .add(inputs)
          .add(new Custom( "button" ).set( "type", "submit").add( ""+name));

//          .add(new fieldset()
//            .add(new legend(name))
//            .add( inputs )
//            .add(new input().submit().value(name)
//            ));
    }

}
