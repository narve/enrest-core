package no.dv8.eks.controllers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;

public class CRUD<T> extends Controller<T> {

//    @PersistenceContext
    EntityManager em;
    static EntityManagerFactory emf;
    final Class<T> clz;


    public CRUD(Class<T> clz) {
        this.clz = clz;
    }

    public static EntityManagerFactory getEMF() {
        if (emf == null)
            emf = Persistence.createEntityManagerFactory("Eks");
        return emf;
    }

    public static <T> CRUD<T> create(Class<T> clz) {
        return new CRUD<T>(clz);
    }

    public EntityManager getEM() {
        if (em == null) {
            EntityManager myEM = getEMF().createEntityManager();
            em = myEM;
        }
        return em;
    }

    @Override
    public Class<T> getClz() {
        return clz;
    }

    @Override
    public List<T> all() {
        return getEM()
          .createQuery("SELECT x FROM " + clz.getSimpleName() + " x", clz)
          .getResultList();
    }

    @Override
    public T insert(T t) {
        if (!getEM().getTransaction().isActive()) getEM().getTransaction().begin();
        getEM().persist(t);
        getEM().getTransaction().commit();
        return t;
    }

    @Override
    public T update(T t) {
        if (!getEM().getTransaction().isActive()) getEM().getTransaction().begin();
        t = getEM().merge(t);
        getEM().getTransaction().commit();
        return t;
    }

    @Override
    public void deleteById(String t) {
        if (!getEM().getTransaction().isActive()) getEM().getTransaction().begin();
        T e = getEM().find(clz, Long.parseLong(t));
        Objects.requireNonNull(e, "Not found: " + clz.getSimpleName() + "#" + t);
        getEM().remove(e);
        getEM().getTransaction().commit();
    }


}
