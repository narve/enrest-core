package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.enrest.mutation.Resource;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.mutation.Mutator;
import no.dv8.enrest.mutation.FormHelper;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;

import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.eks.rest.EksIndex.resources;
import static no.dv8.enrest.mutation.FormHelper.pathToCreators;

@Slf4j
public class EksForms {

    final String basePath;

    public EksForms(String basePath) {
        this.basePath = basePath;
    }

    public Element executeCreate(String name, HttpServletRequest req) {
        Resource r = locateByRel(name);
        Mutator cr = r.creator();
        Object createResult;
        try {
            createResult = cr.setProps(r.clz().newInstance(), req);
            createResult = cr.create(createResult);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Element e = new EksResources(basePath).toElement(createResult);
        return new div()
          .add(new h1("The object:"))
          .add(e)
          ;
    }

    public form createForm(String name) {
        return FormHelper.createForm(name, locateByRel(name).creator().inputs(null), basePath, "post");
    }

    public form editForm(String substring) {

        Object item = EksResources.getItem(substring);

        String itemType = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        log.info("Edit form for {} {}", itemType, itemId);

        String name = Types.edit.toString();
        Mutator resource = locateByClz(itemType);

        String id = EksResources.itemId(item);

        form f = FormHelper.createForm(Types.edit.toString(), resource.inputs(item), basePath, "post");
        f.add(new input().type("text").id("id").name("id").value(id));

        f.action(new EksResources(basePath).viewUrlForItem(item));
        f.add(new label("action: " + f.get("action")));
        return f;
    }

    public Resource locateByRel(String name) {
        log.info("Locating UserMutator for {}", name);
        return resources().stream().filter(cr -> cr.getName().equals(name)).findFirst().get();
    }

    public Mutator locateByClz(String name) {
        log.info("Locating UserMutator for {}", name);
        return resources().stream().filter(cr -> cr.clz().getSimpleName().equalsIgnoreCase(name)).findFirst().get().creator();
    }

    public ul creatorForms() {
        ul list = new ul();
        resources()
          .stream()
          .map(cr -> relToA(cr.getName(), pathToCreators + "/"))
          .map(a -> new li().add(a))
          .forEach(list::add);
        return list;
    }

}
