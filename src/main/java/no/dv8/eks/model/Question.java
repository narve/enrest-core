package no.dv8.eks.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Question extends ModelBase{

    String title, questionText, answerText;

    public Question(String q, String a) {
        this.questionText = q;
        this.answerText = a;
    }


    @Override
    public String toString() {
        return title;
    }
}
