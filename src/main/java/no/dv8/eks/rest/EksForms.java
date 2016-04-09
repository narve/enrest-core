package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.rest.creators.CreateQuestion;
import no.dv8.eks.rest.creators.CreateUser;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Rels;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.enrest.creators.FormHelper;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksHTML.relToA;
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
        Element createResult = cr.handle(req);
        return new div()
          .add(new h1("Named props"))
          .add(propList)
          .add(new h1("The object:"))
          .add(createResult)
          ;
    }

    form createForm( String name ) {
        return FormHelper.createForm(name, locate(name).inputs());
    }

    CreatorResource locate( String name ) {
        return creators.stream().filter( cr -> cr.getName().equals( name ) ).findFirst().get();
    }

    public ul formsAsList() {
        ul list = new ul();
        creators
          .stream()
          .map( cr -> relToA(cr.getName(), pathToCreators + "/"))
          .map( a -> new li().add( a ) )
          .forEach(list::add );
        return list;
    }

}
