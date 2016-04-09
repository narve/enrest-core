package no.dv8.eks.controllers;

import lombok.ToString;
import no.dv8.eks.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Users {

    static Users instance = new Users();
    public static Users instance() { return instance; }

    List<User> users = new ArrayList<>( asList( new User( "narve", "narve@dv8.no")));

    public List<User> getUsers() {
        return users;
    }

    public User add(User u ) {
        u.setId(UUID.randomUUID().toString());
        users.add( u );
        return u;
    }

    public Collection<User> search(String term) {
        return getUsers().stream().filter( u -> u.toString().contains(term)).collect( toList());
    }
}
