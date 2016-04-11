package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.rest.resources.QuestionResource;
import no.dv8.eks.rest.resources.UserResource;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.enrest.creators.FormHelper;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.enrest.creators.FormHelper.pathToCreators;

@Slf4j
@Stateless
public class EksForms {

    @Inject
    UserResource userRes = new UserResource();

    @Inject
    QuestionResource questionRes = new QuestionResource();

//    @Inject
//    EksResources eksResources = new EksResources();

    public List<CreatorResource> creators() {
        return asList(
          userRes.creator(),
          questionRes.creator()
        );
    }

    public Element executeCreate(String name, HttpServletRequest req) {
        CreatorResource cr = locateByRel(name);
        Object createResult;
        try {
            createResult = cr.setProps(cr.clz().newInstance(), req);
            createResult = cr.create(createResult);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Element e = EksResources.toElement(createResult);
        return new div()
          .add(new h1("The object:"))
          .add(e)
          ;
    }

    public form createForm(String name) {
        return FormHelper.createForm(name, locateByRel(name).inputs(null), "post");
    }

    public form editForm(String substring) {

        Object item = EksResources.getItem(substring);

        String itemType = substring.split("/")[0];
        String itemId = substring.split("/")[1];
        log.info("Edit form for {} {}", itemType, itemId);

        String name = Types.edit.toString();
        CreatorResource resource = locateByClz(itemType);

        String id = EksResources.itemId(item);

        form f = FormHelper.createForm(Types.edit.toString(), resource.inputs(item), "post");
        f.add(new input().type("text").id("id").name("id").value(id));

        f.action(EksResources.viewUrlForItem(item));
        f.add(new label("action: " + f.get("action")));
        return f;
    }

    public CreatorResource locateByRel(String name) {
        log.info("Locating CreatorResource for {}", name);
        return creators().stream().filter(cr -> cr.getName().equals(name)).findFirst().get();
    }

    public CreatorResource locateByClz(String name) {
        log.info("Locating CreatorResource for {}", name);
        return creators().stream().filter(cr -> cr.clz().getSimpleName().equalsIgnoreCase(name)).findFirst().get();
    }

    public ul creatorForms() {
        ul list = new ul();
        creators()
          .stream()
          .map(cr -> relToA(cr.getName(), pathToCreators + "/"))
          .map(a -> new li().add(a))
          .forEach(list::add);
        return list;
    }

}
