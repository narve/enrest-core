package no.dv8.eks.rest.creators;

import no.dv8.eks.controllers.Users;
import no.dv8.eks.model.User;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.creators.CreatorResource;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.p;
import no.dv8.xhtml.generation.elements.textarea;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.enrest.creators.FormHelper.text;

public class CreateUser implements CreatorResource<User> {
    @Override
    public List<Element<?>> inputs(User u) {
        return asList(
          text(Names.user).value(u==null?null:u.getUser()),
          text(Names.email).value(u==null?null:u.getEmail()),
          text(Names.name).value(u==null?null:u.getName()),
          text(Names.website).value(u==null?null:u.getWebsite()),
          new input().password().name(Names.password).id(Names.password).value(u==null?null:u.getPassword()),
          new input().file().name(Names.user_image).id(Names.user_image).value(u==null?null:u.getUserImage())
        );
    }

    @Override
    public User create(User user) {
        return Users.instance().add(user);
    }

    @Override
    public User update(User user) {
        return user;
    }

    @Override
    public User setProps(User u, HttpServletRequest req ) {
        u.setName(req.getParameter(Names.name.toString()));
        u.setEmail(req.getParameter(Names.email.toString()));
        u.setPassword(req.getParameter(Names.password.toString()));
        u.setUser(req.getParameter(Names.user.toString()));
        u.setUserImage(req.getParameter(Names.user_image.toString()));
        u.setWebsite(req.getParameter(Names.website.toString()));
        return u;
    }

    @Override
    public String getName() {
        return Types.user_add.toString();
    }

    @Override
    public Class<User> clz() {
        return User.class;
    }
}
