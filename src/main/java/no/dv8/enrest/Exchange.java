package no.dv8.enrest;

import no.dv8.functions.ServletFunctions;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.p;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Exchange {
    private final HttpServletRequest req;
    private final HttpServletResponse res;

    private Object entity;
    private List<a> links = new ArrayList<>();

    public Exchange(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
    }

    public String getFullPath() {
        return req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
    }

    public Exchange finish() {
        try {
            getWriter().close();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getMethod() + " " + getFullPath();
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

    public String getHeader(String s) {
        return req.getHeader(s);
    }

    public Exchange withStatus(int httpStatus) {
        setStatus(httpStatus);
        return this;
    }

    public void setStatus(int httpStatus) {
        res.setStatus(httpStatus);
    }

    public String getMethod() {
        return req.getMethod();
    }

    public String getPathInfo() {
        return req.getPathInfo();
    }

    public PrintWriter getWriter() throws IOException {
        return res.getWriter();
    }

    public Exchange withContentType(String s) {
        setContentType(s);
        return this;
    }

    public void setContentType(String s) {
        res.setContentType(s);
    }

    public Exchange withCharacterEncoding(String s) {
        setCharacterEncoding(s);
        return this;
    }

    public void setCharacterEncoding(String s) {
        res.setCharacterEncoding(s);
    }

    public Map<String, String[]> getParameterMap() {
        return req.getParameterMap();
    }

    public InputStream getInputStream() {
        try {
            return req.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}