package no.dv8.eks.model;


import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Data
@FieldDefaults( level = AccessLevel.PRIVATE)
@MappedSuperclass
public abstract class ModelBase implements Serializable {

    @Id
    @GeneratedValue
    Long id;

}
