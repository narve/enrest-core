package no.dv8.eks.controllers;

import no.dv8.eks.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Questions implements Controller<Question>{

    static Questions instance = new Questions();
    public static Questions instance() { return instance; }

    List<Question> questions = new ArrayList<>();

    public Questions() {
        add(new Question( "hva er meningen?", "42"));
    }

    public Class<Question> getClz() {
        return Question.class;
    }

    public List<Question> all() {
        return questions;
    }

    public Question add(Question u ) {
        u.setId(new Random().nextLong());
        questions.add( u );
        return u;
    }

}
