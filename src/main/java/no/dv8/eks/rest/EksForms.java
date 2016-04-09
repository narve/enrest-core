package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.Questions;
import no.dv8.eks.controllers.Users;
import no.dv8.eks.model.Question;
import no.dv8.eks.model.User;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Rels;
import no.dv8.eks.semantic.Types;
import no.dv8.xhtml.generation.attributes.Id;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.generation.support.ElementBase;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.eks.rest.EksHTML.relToA;

@Slf4j
public class EksForms {

    input text(Names r ) {
        return new input().text().id(r).name(r).placeholder(r.toString());
    }

    div control( Names r ) {
        return control( text(r), r );
    }

    div control(ElementBase<?> element, Names r ) {
        return new div()
        .add( new label().add( r.toString() ).forId( element.get("id").toString() ) )
          .add( element );
    }

    Element userAdd(HttpServletRequest req) {
        ul l = new ul();
        for( Names n: Names.values() ) {
            l.add( new li().add( n + ": " + req.getParameter(n.toString())));
        }

        User u = new User();
        u.setName(req.getParameter(Names.name.toString() ) );
        u.setEmail(req.getParameter(Names.email.toString() ) );
        Users.instance().add(u);

        return new div()
          .add( new h1( "Named props" ) )
          .add( l )
          .add( new h1( "The object:"))
          .add( new p( u.toString() ) )
          ;
    }

    Element questionAdd(HttpServletRequest req) {
        ul l = new ul();
        for( Names n: Names.values() ) {
            l.add( new li().add( n + ": " + req.getParameter(n.toString())));
        }

        Question u = new Question();
        u.setQuestionText(req.getParameter(Names.question_text.toString() ) );
        u.setAnswerText(req.getParameter(Names.answer_text.toString() ) );
        Questions.instance().add(u);

        return new div()
          .add( new h1( "Named props" ) )
          .add( l )
          .add( new h1( "The object:"))
          .add( new p( u.toString() ) )
          ;
    }

    form userAddForm() {
        List<Element<?>> inputs = asList(
          control( Names.user),
          control(Names.email),
          control(Names.name),
          control(Names.website),
          control(new input().password().name(Names.password).id( Names.password), Names.password),
          control(new input().file().name(Names.user_image).id( Names.user_image), Names.user_image),
          control(new textarea().name(Names.description).id( Names.description), Names.description)
        );

        return new form()
          .clz(Types.user_add)
          .post()
          .action( "/eks/" + "form-actions/" + Types.user_add)
          .add(new fieldset()
            .add(new legend(Types.user_add))
            .add( inputs )
            .add(new input().submit().value(Types.user_add)
            ));
    }


    form questionAddForm() {
        List<Element<?>> inputs = asList(
          control( Names.question_text),
          control(Names.answer_text)
        );

        return new form()
          .clz(Types.user_add)
          .post()
          .action( "/eks/" + "form-actions/" + Types.question_add)
          .add(new fieldset()
            .add(new legend(Types.question_add))
            .add( inputs )
            .add(new input().submit().value(Types.question_add)
            ));
    }

    public form form(String name) {
        String clzName = name.replaceAll("\\-", "\\_");
        log.info("Clzname: {}", clzName);
        Types clz = Types.valueOf(clzName);
        switch (clz) {
            case questions_search:
            case users_search:
            case messages_search:
                return searchForm(clz);
            case question_add:
                return questionAddForm();
            case user_add:
                return userAddForm();
            default:
                throw new UnsupportedOperationException("Unknown form: " + name );
        }
    }

    public form searchForm(Types rel) {
        return new form()
          .clz(rel)
          .get()
          .action( "/eks/queriesAsList/"+rel )
          .add(
            new fieldset()
              .add(new legend(rel.toString()))
              .add(control( new input().text().name(Names.search).id( Names.search), Names.search))
              .add(new input().submit().value(rel))
          );
    }

    public Element formAction(String form, HttpServletRequest req ) {
//        return new section().add( new h1("form-action") );
        Types type = Types.valueOf( form.replaceAll( "-", "_" ) );
        switch( type ) {
            case question_add:
                return questionAdd( req );
            case user_add:
                return userAdd( req );
            default:
                throw new RuntimeException("unknown form: " + type );
        }
    }


    public ul formsAsList() {
        return new ul()
          .add( new li().add(relToA(Rels.user_add, "forms/")))
          .add( new li().add(relToA(Rels.question_add, "forms/")));
    }
}
