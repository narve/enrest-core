package no.dv8.eks.rest.resources;

import no.dv8.eks.controllers.Questions;
import no.dv8.eks.model.Question;
import no.dv8.eks.model.User;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Rels;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.mutation.Locator;
import no.dv8.enrest.mutation.Mutator;
import no.dv8.enrest.mutation.Resource;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.textarea;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

public class QuestionResource implements Resource<Question> {

    @Override
    public Mutator<Question> creator() {
        return new QuestionMutator();
    }

    @Override
    public Locator<Question> locator() {
        return s -> questions().getById(s);
    }

    @Override
    public Mutator<Question> updater() {
        return new QuestionMutator();
    }

    @Override
    public List<QueryResource> queries() {
        return asList(
          new SimpleQuery<Question>(Rels.questions_search.toString(), s -> search(s))
        );
    }

    public Collection<Question> search(String s) {
        return questions().search(s);
    }

    Questions questions() {
        return new Questions();
    }

    public Question getById(String itemId) {
        return questions().getById(itemId);
    }

    public List<Element<?>> inputs(Question q) {
        if( q == null ) q = new Question();
        return asList(
          new input().text().name( Names.title ).id( Names.title ).value( q.getTitle() ),
          new textarea().name(Names.question_text).id(Names.question_text).add(q == null ? null : q.getQuestionText()),
          new textarea().name(Names.answer_text).id(Names.answer_text).add(q == null ? null : q.getAnswerText())
        );
    }

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

    public class QuestionMutator implements Mutator<Question> {

        @Override
        public List<Element<?>> inputs(Question q) {
            return QuestionResource.this.inputs(q);
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
        public Question setProps(Question q, HttpServletRequest req) {
            return QuestionResource.this.setProps(q, req);
        }

    }
}
