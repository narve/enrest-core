package no.dv8.eks.rest.resources;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.controllers.UsersJPA;
import no.dv8.eks.model.User;
import no.dv8.eks.semantic.Names;
import no.dv8.eks.semantic.Rels;
import no.dv8.eks.semantic.Types;
import no.dv8.enrest.model.Link;
import no.dv8.enrest.mutation.Linker;
import no.dv8.enrest.mutation.Locator;
import no.dv8.enrest.mutation.Mutator;
import no.dv8.enrest.mutation.Resource;
import no.dv8.enrest.queries.QueryResource;
import no.dv8.enrest.queries.SimpleQuery;
import no.dv8.functions.XBiConsumer;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.link;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static no.dv8.enrest.mutation.FormHelper.text;
import static no.dv8.functions.ServletFunctions.consumer;


@Slf4j
public class UserResource implements Resource<User> {

    public UserResource() {
    }

    UsersJPA users() {
        return new UsersJPA();
    }

    @Override
    public List<QueryResource> queries() {
        return asList(
          new SimpleQuery<User>(Rels.users_search.toString(), s -> search(s))
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
    public Locator<User> locator() {
        return s -> users().getById(s);
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
        return User.class.getSimpleName();
    }

    public XBiConsumer<HttpServletRequest, HttpServletResponse> testBIC() {
        Function<HttpServletRequest, Long> idExtractor = req -> Long.parseLong( req.getParameterMap().get("id")[0]);
//        Function<HttpServletRequest, User> mapper = idExtractor.andThen( id -> users().all().get(0));
        Function<HttpServletRequest, User> mapper =
          idExtractor
            .andThen( id -> users().getById(id))
            .andThen( u -> { log.info( "User: {}", u.getName()); return u;});
        return consumer( mapper );
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

    @Override
    public Linker<User> linker() {
        a self = new a()
          .rel( "self" )
            .self()
          .href("thehgref")
          .add( "content" );
        return u -> asList(
          self
        );
    }
}
