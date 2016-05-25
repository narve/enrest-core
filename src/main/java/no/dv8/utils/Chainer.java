package no.dv8.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;

public class Chainer<T> {

    private List<UnaryOperator<T>> chain = new ArrayList<>();

    private static <T> UnaryOperator<T> chain(List<UnaryOperator<T>> chain) {
        return
          chain
            .stream()
            .reduce(UnaryOperator.identity(), (a, b) -> s -> b.apply(a.apply(s)));
    }

    public UnaryOperator<T> chain() {
        return chain(chain);
    }

    public Chainer add(UnaryOperator<T>... items) {
        chain.addAll(asList(items));
        return this;
    }

    public Chainer add(Function<T, T>... items) {
        for (Function<T, T> f : items) {
            chain.add(x -> f.apply(x));
        }
        return this;
    }
}
