package no.dv8.eks.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.dv8.utils.Strings;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

import static no.dv8.utils.Strings.isNullOrEmpty;

@Entity
@XmlRootElement
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends ModelBase {
    String name, email, user, userImage, website, password;

    public User(String name, String email) {
        this.name = name; this.email = email;
    }

    @Override
    public String toString() {
        return isNullOrEmpty(name ) ? super.toString() : name;
    }
}
