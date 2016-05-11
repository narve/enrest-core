package no.dv8.enrest.resources;

import java.util.Optional;

@FunctionalInterface
public interface Locator<T> {
    Optional<T> getById(String id );
}
