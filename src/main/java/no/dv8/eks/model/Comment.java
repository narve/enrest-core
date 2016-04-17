package no.dv8.eks.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends ModelBase {

    String comment;
    @ManyToOne( optional = false)
    User submitter;
    @ManyToOne( optional = false)
    Article article;

}
