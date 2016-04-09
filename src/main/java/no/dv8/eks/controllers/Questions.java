package no.dv8.eks.controllers;

import no.dv8.eks.model.Question;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Questions {

    static Questions instance = new Questions();
    public static Questions instance() { return instance; }

    List<Question> questions = new ArrayList<>( asList( new Question( "hva er meningen?", "42")));

    public List<Question> getQuestions() {
        return questions;
    }

    public Question add(Question u ) {
        u.setId(UUID.randomUUID().toString());
        questions.add( u );
        return u;
    }
    public Collection<Question> search(String term) {
        return getQuestions().stream().filter( u -> u.toString().contains(term)).collect( toList());
    }

}
