package no.dv8.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;

public class Chainer<T> implements UnaryOperator<T> {

    private List<UnaryOperator<T>> chain = new ArrayList<>();
    private UnaryOperator<T> composite = UnaryOperator.identity();

    private static <T> UnaryOperator<T> chain(List<UnaryOperator<T>> chain) {
        return
          chain
            .stream()
            .reduce(UnaryOperator.identity(), (a, b) -> s -> b.apply(a.apply(s)));
    }

    @Override
    public T apply(T t) {
        return composite.apply(t);
    }

    public Chainer add(UnaryOperator<T>... items) {
        chain.addAll(asList(items));
        composite = chain(chain);
        return this;
    }

    public Chainer add(Function<T, T>... items) {
        for( Function<T,T> f: items ) {
            chain.add( x -> f.apply(x) );
        }
        composite = chain(chain);
        return this;
    }
}
