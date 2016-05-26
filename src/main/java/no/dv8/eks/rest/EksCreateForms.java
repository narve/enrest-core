package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.Rels;
import no.dv8.enrest.Exchange;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.functions.XFunction;
import no.dv8.functions.XUnaryOperator;
import no.dv8.utils.Props;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static no.dv8.eks.rest.EksHTML.relToA;

@Slf4j
public class EksCreateForms implements Predicate<Exchange>, UnaryOperator<Exchange> {

    final EksResources resources;

    public EksCreateForms(EksResources resources) {
        this.resources = resources;
    }

    public Element executeCreate(Resource<?> resource, HttpServletRequest req) {
        Mutator cr = resource.creator();
        Object createResult;
        try {
            Map<String, String> vals = new Props().single(req.getParameterMap());
            createResult = cr.setProps(resource.clz().newInstance(), vals);
            createResult = cr.create(createResult);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Element e = resources.toElement(createResult);
        return new div()
          .add(new h1("The object:"))
          .add(e);
    }

    public form editForm(Mutator resource, Object item) {
        form f = createForm(Rels.edit, resource.inputs(item), "post");
        f.action(resources.urlCreator.viewItem(resources.itemClass(item), resources.itemId(item)));
        return f;
    }

    public ul creatorForms() {
        ul list = new ul();
        resources.resources()
          .stream()
          .map(cr -> relToA(cr.getName(), resources.urlCreator.createForm(cr.getName())))
          .map(a -> new li().add(a))
          .forEach(list::add);
        return list;
    }

    @Override
    public boolean test(Exchange x) {
        return resources.urlCreator.isCreateForm((x.getFullPath()));
    }

    @Override
    public Exchange apply(Exchange x) {
        try {
            String path = x.getFullPath();
            String type = resources.urlCreator.type(path);
            Resource res = resources.locateByRel(type).get();
            Object obj = res.clz().newInstance();
            return x.withEntity(createForm(type, res.creator().inputs(obj), "post"));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public static input text(Object r ) {
        return new input().text().id(r).name(r).placeholder(r.toString());
    }

    public form createForm(Object name, List<Element<?>> inputs, Object method ) {
        return new form()
          .clz(name)
          .method(method)
          .action( resources.urlCreator.createAction( name ))
          .set( "accept-charset", "utf-8")
          .set( "enc-type", "application/x-www-form-urlencoded")
          .add(new fieldset()
            .add(new legend(name))
            .add( inputs )
            .add(new input().submit().value(name)
            ));
    }

}
