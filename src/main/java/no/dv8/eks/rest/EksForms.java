package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.resources.Resource;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.resources.Mutator;
import no.dv8.enrest.resources.FormHelper;
import no.dv8.reflect.Props;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.enrest.resources.FormHelper.pathToCreators;

@Slf4j
public class EksForms {

    final EksResources resources;

    public EksForms(EksResources resources) {
        this.resources = resources;
    }

    public Element executeCreate(String name, HttpServletRequest req) {
        Resource r = resources.locateByRel(name).get();
        Mutator cr = r.creator();
        Object createResult;
        try {
            Map<String, String> vals = new Props().single(req.getParameterMap());
            createResult = cr.setProps(r.clz().newInstance(), vals);
            createResult = cr.create(createResult);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Element e = resources.toElement(createResult);
        return new div()
          .add(new h1("The object:"))
          .add(e)
          ;
    }

    public form createForm(String name) {
        try {
            Resource res = resources.locateByRel(name).get();
            Object obj = res.clz().newInstance();
            return FormHelper.createForm(name, res.creator().inputs(obj), resources.basePath, "post");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public form editForm(Object item, String substring) {

        String itemType = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        log.info("Edit form for {} {}", itemType, itemId);

        String name = Types.edit.toString();
        Mutator resource = resources.locateByName(itemType).get().updater();

        String id = EksResources.itemId(item);

        form f = FormHelper.createForm(Types.edit.toString(), resource.inputs(item), resources.basePath, "post");
//        f.add(new input().type("text").id("id").name("id").value(id));
//
        f.action(resources.viewUrlForItem(item));
//        f.add(new label("action: " + f.get("action")));
        return f;
    }

    public ul creatorForms() {
        ul list = new ul();
        resources.resources()
          .stream()
          .map(cr -> relToA(cr.getName(), resources.basePath + "/" + pathToCreators + "/"))
          .map(a -> new li().add(a))
          .forEach(list::add);
        return list;
    }

}
