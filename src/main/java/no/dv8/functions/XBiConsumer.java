package no.dv8.functions;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface XBiConsumer<T, U> {
    void accept(T t, U u) throws Exception;

    static <T,U> BiConsumer<T, U> hidex( XBiConsumer<T, U> in ) {
        return (t,u) -> {
            try {
                in.accept( t,u );
            } catch( RuntimeException re ) {
                throw re;
            } catch( Exception e ) {
                throw new RuntimeException(e);
            }
        };
    }
}
