package no.dv8.eks.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    String id, name, email, user, userImage, website, password;

    public User(String name, String email) {
        this.name = name; this.email = email;
    }

    @Override
    public String toString() {
        return name;
    }
}
