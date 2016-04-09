package no.dv8.enrest.spi;

public interface RelTypeProvider {

    default String reltype( Class<?> clz ) {
        return clz.getName();
    }
}
