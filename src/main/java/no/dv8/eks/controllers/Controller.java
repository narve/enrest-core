package no.dv8.eks.controllers;

import java.util.List;

import static java.util.stream.Collectors.toList;

public interface Controller<T> {

    Class<T> getClz();

    List<T> all();

    T add( T t );

    default Object getId(T t) {
        try {
            return t.getClass().getMethod("getId").invoke(t).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default T getById(Object id) {
        List<T> collect = all().stream().filter(t -> getId(t).equals(id)).collect(toList());
        if (collect.isEmpty())
            return null;
        else if (collect.size() > 1)
            throw new IllegalStateException("multipe hits");
        else
            return collect.get(0);
    }

    default List<T> search(String s) {
        return all().stream().filter(t -> t.toString().toLowerCase().contains(s.toLowerCase())).collect(toList());
    }

}
