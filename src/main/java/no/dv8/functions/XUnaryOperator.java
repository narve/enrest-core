package no.dv8.functions;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface XUnaryOperator<T> {
    T apply(T in) throws Exception;

    static <T> UnaryOperator<T> hidex(XUnaryOperator<T> in) {
        return t -> {
            try {
                return in.apply(t);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
