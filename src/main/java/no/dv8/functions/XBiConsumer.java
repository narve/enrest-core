package no.dv8.functions;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface XBiConsumer<T, U> {
    void accept(T t, U u) throws Exception;

    static <T,U> BiConsumer<T, U> hidex( String name, XBiConsumer<T, U> in ) {
        return new BiConsumer<T, U>() {
            @Override
            public void accept(T t, U u) {
                try {
                    in.accept( t,u );
                } catch( RuntimeException re ) {
                    throw re;
                } catch( Exception e ) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String toString() {
                return name == null ? in.toString() : name;
            }
        };
    }
    static <T,U> BiConsumer<T, U> hidex( XBiConsumer<T, U> in ) {
        return hidex( null, in );
    }
}
