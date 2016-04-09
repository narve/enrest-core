package no.dv8.eks.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    String id, questionText, answerText;

    public Question(String q, String a) {
        this.questionText = q;
        this.answerText = a;
    }


    @Override
    public String toString() {
        return questionText;
    }
}
