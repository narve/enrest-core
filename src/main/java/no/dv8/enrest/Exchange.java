package no.dv8.enrest;

import no.dv8.xhtml.generation.elements.p;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Exchange {
    public final HttpServletRequest req;
    public final HttpServletResponse res;

    private Object entity;

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

    public Object getEntity() {
        return entity;
    }


    public Exchange withEntity(Object test) {
        this.entity = test;
        return this;
    }
}