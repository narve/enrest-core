package no.dv8.eks.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Article extends ModelBase {
    String title = "article title";
    String keywords = "keywords";
    String content = "content";

    long length = 123;

    @OneToMany
    List<Comment> comments;

    Question question;

    Comment comment;

    @Override
    public String toString() {
        return "#" + getId() + " " + title;
    }
}
