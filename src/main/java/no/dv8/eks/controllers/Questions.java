package no.dv8.eks.controllers;

import no.dv8.eks.model.Question;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Stateless
public class Questions extends Controller<Question> {

    @PersistenceContext
    static EntityManager em;

    public Questions() {
    }

    public Questions(EntityManager em) {
        this.em = em;
    }

    public EntityManager getEM() {
        if (em == null) {
            EntityManager myEM = Persistence.createEntityManagerFactory("Eks").createEntityManager();
            em = myEM;
        }
        return em;
    }

    public Class<Question> getClz() {
        return Question.class;
    }

    public List<Question> all() {
        return getEM()
          .createQuery("SELECT x FROM Question x", Question.class)
          .getResultList();
    }

    public Question insert(Question u) {
        getEM().getTransaction().begin();
        getEM().persist(u);
        getEM().getTransaction().commit();
        return u;
    }

    public List<Question> search(String s) {
        return all().stream().filter(t -> t.toString().toLowerCase().contains(s.toLowerCase())).collect(toList());
    }

    @Override
    public Question update(Question question) {
        getEM().getTransaction().begin();
        question = getEM().merge(question);
        getEM().getTransaction().commit();
        return question;
    }

    @Override
    public void deleteById(String t) {
        getEM().getTransaction().begin();
        getEM().remove(em.find(Question.class, t));
        getEM().getTransaction().commit();
    }

}
