package no.dv8.enrest.resources;

@FunctionalInterface
public interface Locator<T> {
    T getById( String id );
}
