package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.model.Question;
import no.dv8.eks.rest.creators.CreateQuestion;
import no.dv8.eks.rest.creators.CreateUser;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.enrest.creators.FormHelper;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksHTML.relToA;
import static no.dv8.eks.rest.EksServlet.basePath;
import static no.dv8.enrest.creators.FormHelper.pathToCreators;

@Slf4j
public class EksForms {

    List<CreatorResource> creators = asList(
      new CreateUser(),
      new CreateQuestion()
    );

    Element executeForm(String name, HttpServletRequest req) {
        CreatorResource cr = locate(name);
        ul propList = new ul();
        for (Names n : Names.values()) {
            propList.add(new li().add(n + ": " + req.getParameter(n.toString())));
        }
        Object createResult = cr.handle(req);
        Element e = new EksResources().toElement(createResult);
        return new div()
          .add(new h1("Named props"))
          .add(propList)
          .add(new h1("The object:"))
          .add(e)
          ;
    }

    form createForm( String name ) {
        return FormHelper.createForm(name, locate(name).inputs(null), "post");
    }
    form editForm( String substring ) {

        Object item = new EksResources().getItem(substring);

//        String itemType = substring.split( "/")[0];
//        String itemId = substring.split( "/")[1];
        log.info( "Edit form for {}", substring );
        String name = Types.edit.toString();
        form f = FormHelper.createForm(Types.edit.toString(), locate(Types.question_add.toString()).inputs(item), "post");
        f.add( new input().type("text").id("id").name("id").value(((Question)item).getId()));

          f.action( new EksResources().viewUrlForItem(item));
        f.add( new label("action: " + f.get("action")));
        return f;
    }

    CreatorResource locate( String name ) {
        log.info( "Locating CreatorResource for {}", name );
        return creators.stream().filter( cr -> cr.getName().equals( name ) ).findFirst().get();
    }

    public ul creatorForms() {
        ul list = new ul();
        creators
          .stream()
          .map( cr -> relToA(cr.getName(), pathToCreators + "/"))
          .map( a -> new li().add( a ) )
          .forEach(list::add );
        return list;
    }

}
