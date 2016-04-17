package no.dv8.enrest.mutation;

@FunctionalInterface
public interface Locator<T> {
    T getById( String id );
}
