package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.Rels;
import no.dv8.enrest.forms.FormHelper;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.Resource;
import no.dv8.utils.Props;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static no.dv8.eks.rest.EksHTML.relToA;

@Slf4j
public class EksForms {

    final EksResources resources;

    public EksForms(EksResources resources) {
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

    public form createForm(String name) {
        try {
            Resource res = resources.locateByRel(name).get();
            Object obj = res.clz().newInstance();
            FormHelper fh = new FormHelper(this.resources);
            return fh.createForm(name, res.creator().inputs(obj), "post");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public form editForm(Mutator resource, Object item) {
        FormHelper fh = new FormHelper(this.resources);
        form f = fh.createForm(Rels.edit, resource.inputs(item), "post");
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

}
