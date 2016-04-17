package no.dv8.eks.controllers;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class Controller<T> {

    public abstract Class<T> getClz();

    public abstract List<T> all();

    public abstract T insert(T t );

    public Object getId(T t) {
        try {
            return t.getClass().getMethod("getId").invoke(t).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T getById(Object id) {
        List<T> collect = all().stream().filter(t -> getId(t).toString().equals(id)).collect(toList());
        if (collect.isEmpty())
            return null;
        else if (collect.size() > 1)
            throw new IllegalStateException("multipe hits");
        else
            return collect.get(0);
    }

    public  List<T> search(String s) {
        return all().stream().filter(t -> t.toString().toLowerCase().contains(s.toLowerCase())).collect(toList());
    }

    public abstract T update(T question);
}
