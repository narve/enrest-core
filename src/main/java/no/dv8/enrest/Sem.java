package no.dv8.enrest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
public @interface Sem {
    String name() default "";
    String url() default "";
    String vocab() default "";
}
