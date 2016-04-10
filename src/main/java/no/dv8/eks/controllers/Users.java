package no.dv8.eks.controllers;

import lombok.ToString;
import no.dv8.eks.model.User;
import no.dv8.xhtml.generation.support.Element;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Users implements Controller<User>{

    static Users instance = new Users();
    public static Users instance() { return instance; }

    public Users() {
        add( new User( "narve sætre", "narve@dv8.no"));
        add( new User( "edita", "edita@dv8.no"));
    }

    public Class<User> getClz() {
        return User.class;
    }

    List<User> users = new ArrayList<>();

    public List<User> all() {
        return users;
    }

    public User add(User u ) {
        u.setId(new Random().nextLong());
        users.add( u );
        return u;
    }
//
//    public List<User> search(String term) {
//        return getUsers().stream().filter( u -> u.toString().contains(term)).collect( toList());
//    }
//
//    public User getById(String itemId) {
//        return getUsers().stream().filter( u -> u.getId().equals(itemId)).findFirst().get();
//    }
}
