package no.dv8.eks.rest.resources;

import no.dv8.eks.controllers.Questions;
import no.dv8.eks.controllers.UsersJPA;
import no.dv8.eks.model.Question;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.textarea;
import no.dv8.xhtml.generation.support.Element;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

public class QuestionResource {

    @PersistenceContext
    EntityManager em;

    public CreatorResource<Question> creator() {
        return new Creator();
    }

    public Collection<Question> search(String s) {
        return questions().search(s);
    }

    Questions questions() {
        return new Questions(em);
    }

    public Question getById(String itemId) {
        return questions().getById(itemId);
    }

    public class Creator implements CreatorResource<Question> {

        @Override
        public List<Element<?>> inputs(Question q) {
            if( q == null ) q = new Question();
            return asList(
              new input().text().name( Names.title ).id( Names.title ).value( q.getTitle() ),
              new textarea().name(Names.question_text).id(Names.question_text).add(q == null ? null : q.getQuestionText()),
              new textarea().name(Names.answer_text).id(Names.answer_text).add(q == null ? null : q.getAnswerText())
            );
        }

        @Override
        public Question create(Question question) {
            questions().insert(question);
            return question;
        }

        @Override
        public Question update(Question question) {
            return questions().update(question);
        }

        @Override
        public Question setProps(Question u, HttpServletRequest req) {
            u.setTitle(req.getParameter(Names.title.toString()));
            u.setQuestionText(req.getParameter(Names.question_text.toString()));
            u.setAnswerText(req.getParameter(Names.answer_text.toString()));
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
}
