package no.dv8.utils;

import javafx.util.Pair;
import no.dv8.functions.XFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static no.dv8.functions.ServletFunctions.exMeansFalse;
import static no.dv8.functions.XFunction.hidex;


public class Forker<T, V> {

    private List<Pair<Predicate<T>, Function<T, V>>> fork = new ArrayList<>();

    public Forker() {
    }

    public Function<T, V> forker() {
        return t -> applyFirstMatch(t);
    }

    public V applyFirstMatch(T x) throws UnsupportedOperationException{
        Optional<Pair<Predicate<T>, Function<T, V>>> handler =
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

    public Forker<T, V> add(String name, Predicate<T> condition, XFunction<T, V> handler) {
        fork.add(new Pair<>(condition, hidex(name, handler)));
        return this;
    }

    public <X extends Predicate<T>&XFunction<T, V>> Forker<T, V> add(String name, X x) {
        return add( name, x, x );
    }

}
