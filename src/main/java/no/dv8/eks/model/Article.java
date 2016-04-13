package no.dv8.eks.model;

import lombok.Data;

import javax.persistence.Entity;

/**
 * Created by narve on 13.04.2016.
 */
@Data
@Entity
public class Article extends ModelBase {
    String title, keywords, content;

    long length;
}
