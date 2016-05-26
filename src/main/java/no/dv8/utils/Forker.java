package no.dv8.utils;

import javafx.util.Pair;
import no.dv8.functions.XFunction;
import no.dv8.functions.XUnaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static no.dv8.functions.ServletFunctions.exMeansFalse;
import static no.dv8.functions.XUnaryOperator.hidex;


public class Forker<T> {

    private List<Pair<Predicate<T>, UnaryOperator<T>>> fork = new ArrayList<>();

    public Forker() {
    }

    public UnaryOperator<T> forker() {
        return t -> applyFirstMatch(t);
    }

    public T applyFirstMatch(T x) throws UnsupportedOperationException{
        Optional<Pair<Predicate<T>, UnaryOperator<T>>> handler =
          fork
            .stream()
            .filter(p -> exMeansFalse(p.getKey()).test(x))
            .findFirst();
        if (handler.isPresent()) {
            return handler.get().getValue().apply(x);
        } else {
            throw new UnsupportedOperationException("No handler for " + x);
        }
    }

    public Forker<T> add(String name, Predicate<T> condition, XUnaryOperator<T> handler) {
        fork.add(new Pair<>(condition, hidex(name, handler)));
        return this;
    }

    public <X extends Predicate<T>&XUnaryOperator<T>> Forker<T> add(String name, X x) {
        return add( name, x, x );
    }

    public <X extends Predicate<T>&XUnaryOperator<T>> Forker<T> add(X x) {
        return add( x.toString(), x, x );
    }

}
