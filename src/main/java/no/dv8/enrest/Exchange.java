package no.dv8.enrest;

import no.dv8.eks.rest.EksResources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Exchange {
    public final HttpServletRequest req;
    public final HttpServletResponse res;

    public Exchange(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
    }

    public String getFullPath() {
            return req.getServletPath() + "/" + req.getPathInfo();
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
}