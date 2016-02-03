package no.dv8.rest3;

public interface RelTypeProvider {

    default String reltype( Class<?> clz ) {
        return clz.getName();
    }
}
