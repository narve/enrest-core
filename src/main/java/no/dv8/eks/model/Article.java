package no.dv8.eks.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Article extends ModelBase {
    String title = "articletitle";
    String keywords = "keywords";
    String content = "content";

    long length = 123;

    @Override
    public String toString() {
        return title;
    }
}
