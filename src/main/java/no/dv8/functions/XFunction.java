package no.dv8.functions;

import java.util.function.Function;

@FunctionalInterface
public interface XFunction<T, U> {
    static <T, U> Function<T, U> hidex(String name, XFunction<T, U> in) {
        return new Function<T, U>() {
            @Override
            public String toString() {
                return name != null ? name : in.toString();
            }

            @Override
            public U apply(T t) {
                try {
                    return in.apply(t);
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    U apply(T t) throws Exception;


}
