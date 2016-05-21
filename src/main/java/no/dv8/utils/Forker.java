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


public class Forker<T, V> implements Function<T, V> {

    private List<Pair<Predicate<T>, Function<T, V>>> fork = new ArrayList<>();

    @Override
    public V apply(T x) {
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
//
//    public Forker add(String name, Predicate<T> condition, Consumer<T> handler) {
//        fork.add(new Pair<>(condition, hidex(name, returner( handler))));
//        return this;
//    }
//
//    private <T> Function<T, V> returner(Consumer<T> handler) {
//        return x -> { handler.accept(x); return x;};
//    }

}
