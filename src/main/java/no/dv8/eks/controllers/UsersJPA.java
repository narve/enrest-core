package no.dv8.eks.controllers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.model.User;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class UsersJPA {

//    @PersistenceContext
    EntityManager em;

    @Inject
    public UsersJPA(EntityManager em) {
        this.em = em;
    }

    public User insert(User u ) {
        em.persist(u);
        return u;
    }

    public List<User> all() {
        return em.createQuery( "SELECT x FROM User x", User.class ).getResultList();
    }
}
