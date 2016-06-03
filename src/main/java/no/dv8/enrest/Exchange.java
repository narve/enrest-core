package no.dv8.enrest;

import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.p;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Exchange {
    public final HttpServletRequest req;
    public final HttpServletResponse res;

    private Object entity;
    private List<a> links = new ArrayList<>();

    public Exchange(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
    }

    public String getFullPath() {
        return req.getServletPath() + "/" + (req.getPathInfo() == null ? "" : req.getPathInfo());
    }

    public Exchange finish() {
        try {
            res.getWriter().close();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return req.getMethod() + " " + req.getRequestURL();
    }

    public <T> T getEntity() {
        return (T) entity;
    }


    public Exchange withEntity(Object test) {
        this.entity = test;
        return this;
    }

    public Exchange withLinks(List<a> links) {
        this.links = links;
        return this;
    }

    public List<a> getLinks() {
        return links;
    }

    public String header(String s) {
        return req.getHeader(s);
    }

    public Exchange withStatus(int httpStatus) {
        res.setStatus(httpStatus);
        return this;
    }
}