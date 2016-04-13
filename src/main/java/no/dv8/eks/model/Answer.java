package no.dv8.eks.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults( level = AccessLevel.PRIVATE)
public class Answer extends ModelBase {

    String answer;

    @ManyToOne
    Question question;

}
