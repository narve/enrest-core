package no.dv8.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Optional;

public class OptionalMatcher {
    public static <T> Matcher<Optional<T>> isPresent() {
        return new BaseMatcher<Optional<T>>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof Optional && ((Optional) o).isPresent();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("present");
            }

        };
    }


}
