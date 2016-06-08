package no.dv8.eks.controllers;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.model.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Stateless
@Slf4j
//@NoArgsConstructor
public class UsersJPA {

    @PersistenceContext
    static EntityManager em;

    public UsersJPA() {
    }

    public UsersJPA(EntityManager em) {
        log.info("Created! em={}", em);
        this.em = em;
    }

    EntityManager getEM() {
        if (em == null) {
            EntityManager myEM = Persistence.createEntityManagerFactory("Eks").createEntityManager();
            setEM(myEM);
        }
        return em;
    }

    public void setEM(EntityManager em) {
        this.em = em;
    }

    public User getById(Object id) {
        List<User> collect = all()
          .stream()
          .filter(t -> t.getId().toString().equals(id.toString()))
          .collect(Collectors.toList());
        if (collect.isEmpty())
            return null;
        else if (collect.size() > 1)
            throw new IllegalStateException("multipe hits");
        else
            return collect.get(0);
    }

    //    @Override
    public Class<User> getClz() {
        return User.class;
    }

    public User insert(User u) {
        getEM().getTransaction().begin();
        getEM().persist(u);
        getEM().getTransaction().commit();
        return u;
    }

    public List<User> all() {
        return getEM().createQuery("SELECT x FROM User x", User.class).getResultList();
    }

    public List<User> search(String s) {
        return all()
          .stream()
          .filter(t -> s == null || t.toString().toLowerCase().contains(s.toLowerCase()))
          .collect(toList());
    }

    public User update(User user) {
        getEM().getTransaction().begin();
        user = getEM().merge(user);
        getEM().getTransaction().commit();
        return user;
    }


}
