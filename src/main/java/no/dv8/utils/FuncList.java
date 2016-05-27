package no.dv8.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static no.dv8.functions.ServletFunctions.exMeansFalse;


public class FuncList<T> {

    private List<Pair<Predicate<T>, UnaryOperator<T>>> fork = new ArrayList<>();

    public FuncList() {
    }

    public static <X> Predicate<X> always() {
        return x -> true;
    }

    public UnaryOperator<T> forker() {
        return forker(x -> "No handler for " + String.valueOf(x));
    }

    public UnaryOperator<T> forker(Function<T, String> messageSupplier) {
        return t -> applyFirstMatch(t, messageSupplier);
    }

    public T applyFirstMatch(T x, Function<T, String> messageSupplier) throws UnsupportedOperationException {
        Optional<Pair<Predicate<T>, UnaryOperator<T>>> handler =
          fork
            .stream()
            .filter(p -> exMeansFalse(p.getKey()).test(x))
            .findFirst();
        if (handler.isPresent()) {
            return handler.get().getValue().apply(x);
        } else {
            throw new UnsupportedOperationException(messageSupplier.apply(x));
        }
    }

    public FuncList<T> add(String name, Predicate<T> condition, UnaryOperator<T> handler) {
//        fork.add(new Pair<>(condition, hidex(name, handler)));
        fork.add(new Pair<>(condition, handler));
        return this;
    }

    public <X extends Predicate<T> & UnaryOperator<T>> FuncList<T> add(String name, X x) {
        return add(name, x, x);
    }

    public <X extends Predicate<T> & UnaryOperator<T>> FuncList<T> add(X x) {
        return add(x.toString(), x, x);
    }

    public UnaryOperator<T> all() {
        return obj ->
          fork
            .stream()
            .filter(x -> x.getKey().test(obj))
            .map(p -> p.getValue())
            .reduce(UnaryOperator.identity(),
              (a, b) -> (x -> b.apply(a.apply(x)))
            ).apply(obj);
    }

}
