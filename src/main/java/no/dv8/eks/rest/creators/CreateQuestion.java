package no.dv8.eks.rest.creators;

import no.dv8.eks.controllers.Questions;
import no.dv8.eks.model.Question;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.xhtml.generation.elements.p;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.enrest.creators.FormHelper.text;

public class CreateQuestion implements CreatorResource<Question> {

    @Override
    public List<Element<?>> inputs(Question q) {
        return asList(
          text(Names.question_text).value( q==null?null:q.getQuestionText()),
          text(Names.answer_text).value( q==null?null:q.getAnswerText())
        );
    }

    @Override
    public Question create(Question question) {
        return Questions.instance().add(question);
    }

    @Override
    public Question update(Question question) {
        return question;
    }

    @Override
    public Question setProps(Question u, HttpServletRequest req ) {
        u.setQuestionText(req.getParameter(Names.question_text.toString()));
        u.setAnswerText(req.getParameter(Names.answer_text.toString()));
        Questions.instance().add(u);
        return u;
    }

    @Override
    public String getName() {
        return Types.question_add.toString();
    }

    @Override
    public Class<Question> clz() {
        return Question.class;
    }


}
