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
          text(Names.user),
          text(Names.email),
          text(Names.name),
          text(Names.website),
          new input().password().name(Names.password).id(Names.password),
          new input().file().name(Names.user_image).id(Names.user_image),
          new textarea().name(Names.description).id(Names.description)
        );
    }


    @Override
    public User handle( HttpServletRequest req ) {
        User u = new User();
        u.setName(req.getParameter(Names.name.toString()));
        u.setEmail(req.getParameter(Names.email.toString()));
        Users.instance().add(u);
        return u;
    }

    @Override
    public String getName() {
        return Types.user_add.toString();
    }
}
