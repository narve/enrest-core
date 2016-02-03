package no.dv8.rest2.framework;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransitionBuilder<F, T> {

    Transition<F, T> t;
    RestContainer cnt;

    public TransitionBuilder(RestContainer cnt, Class<F> from, Class<T> to) {
        super();
        this.cnt = cnt;
        t = new Transition<>();
        t.from = from;
        t.to = to;
        t.url = cnt.newUrl();
    }

    public Transition<F, T> buildAndRegister() {
        cnt.transitions().add(t);
        return t;
    }

    public TransitionBuilder<F, T> linkFrom(Class<?> srcClz) {
        t.linkedFrom.add(srcClz);
        return this;
    }

    public TransitionBuilder<F, T> withName(String n) {
        t.name = n;
        return this;
    }

    public TransitionBuilder<F, T> action(Function<F, T> f) {
        t.action = f;
        return this;
    }

    public TransitionBuilder<F, T> delete(Consumer<F> f) {
        t.action = t -> { f.accept(t); return null; };
        return this;
    }

    public TransitionBuilder<F, T> disallowWhen(Predicate<F> f) {
        return this;
    }


}
