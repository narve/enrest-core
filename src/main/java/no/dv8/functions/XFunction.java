package no.dv8.functions;

import java.util.function.Function;

@FunctionalInterface
public interface XFunction<T, U> {
    U apply( T t ) throws Exception;

    default <T,U> Function<T, U> hidex(XFunction<T, U> in ) {
        return t -> {
            try {
                return in.apply(t);
            } catch( RuntimeException re ) {
                throw re;
            } catch( Exception e ) {
                throw new RuntimeException(e);
            }
        };
    }


}
