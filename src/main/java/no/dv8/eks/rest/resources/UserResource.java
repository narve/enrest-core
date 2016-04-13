package no.dv8.eks.rest.resources;

import no.dv8.eks.controllers.UsersJPA;
import no.dv8.eks.model.Question;
import no.dv8.eks.model.User;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Rels;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.mutation.Mutator;
import no.dv8.enrest.mutation.Resource;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.support.Element;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static no.dv8.enrest.mutation.FormHelper.text;


@Stateless
public class UserResource implements Resource<User> {

    public UserResource() {
    }

    UsersJPA users() {
        return new UsersJPA();
    }

    @Override
    public List<QueryResource> queries() {
        return asList(
          new SimpleQuery<User>(Rels.questions_search.toString(), s -> search(s))
        );
    }

    public Collection<User> search(String s) {
        return users().search( s );
    }

    public User getById(String itemId) {
        return users().getById(itemId);
    }

    @Override
    public Mutator<User> creator() {
        return new UserMutator();
    }

    @Override
    public Mutator<User> updater() {
        return new UserMutator();
    }

    @Override
    public Class<User> clz() {
        return User.class;
    }

    @Override
    public String getName() {
        return Types.user_add.toString();
    }

    public class UserMutator implements Mutator<User> {

        @Override
        public List<Element<?>> inputs(User u) {
            return asList(
              text(Names.user).value(u == null ? null : u.getUser()),
              text(Names.email).value(u == null ? null : u.getEmail()),
              text(Names.name).value(u == null ? null : u.getName()),
              text(Names.website).value(u == null ? null : u.getWebsite()),
              new input().password().name(Names.password).id(Names.password).value(u == null ? null : u.getPassword()),
              new input().file().name(Names.user_image).id(Names.user_image).value(u == null ? null : u.getUserImage())
            );
        }

        @Override
        public User create(User user) {
            return users().insert(user);
        }


        @Override
        public User update(User user) {
            return users().update(user);
        }

        @Override
        public User setProps(User u, HttpServletRequest req) {
            u.setName(req.getParameter(Names.name.toString()));
            u.setEmail(req.getParameter(Names.email.toString()));
            u.setPassword(req.getParameter(Names.password.toString()));
            u.setUser(req.getParameter(Names.user.toString()));
            u.setUserImage(req.getParameter(Names.user_image.toString()));
            u.setWebsite(req.getParameter(Names.website.toString()));
            return u;
        }

    }
}
