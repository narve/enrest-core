package no.dv8.rest2.framework;

public class Request {
    public String getUriParam(String id) {
        return "value-of-"+id;
    }

    public String body() {
        return "body";
    }
}
