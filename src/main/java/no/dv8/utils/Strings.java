package no.dv8.utils;

public class Strings {

    public static String notNull(String in) {
        return in == null ? "" : in;
    }
    public static boolean isNullOrEmpty( String in ) {
        return in == null || in.trim().isEmpty();
    }
}
