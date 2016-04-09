package no.dv8.eks.rest.editors;

import no.dv8.eks.controllers.Questions;
import no.dv8.eks.model.Question;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.p;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.enrest.creators.FormHelper.text;

public class EditQuestion implements CreatorResource<Question> {

    @Override
    public boolean isIdempotent() {
        return true;
    }

    @Override
    public List<Element<?>> inputs(Question q) {
        return asList(
          new input().type("text").id("id").name("id").value(q.getId()),
          text(Names.question_text),
          text(Names.answer_text)
        );
    }

    @Override
    public Question handle( HttpServletRequest req ) {
        String id = req.getParameter( Names.id.toString());
        Question u = Questions.instance().getById(id);
        u.setQuestionText(req.getParameter(Names.question_text.toString()));
        u.setAnswerText(req.getParameter(Names.answer_text.toString()));
        return u;
    }

    @Override
    public String getName() {
        return Types.edit.toString();
    }

}
