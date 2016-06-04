package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class MockExchange extends Exchange {

    private String fullPath;
    private String method;
    private String pathInfo;
    private String contentType;
    private String characterEncoding;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String[]> parameters = new HashMap<>();
    private StringWriter writer = new StringWriter();
    private int httpStatus;

    public MockExchange() {
        super(null, null );
    }

    @Override
    public String getFullPath() {
        return this.fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public MockExchange withFullPath(String pathToForm) {
        setFullPath(pathToForm);
        return this;
    }

    public MockExchange withMethod(String x) {
        setMethod(x);
        return this;
    }

    @Override
    public String getMethod() {
        return method; 
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public MockExchange withPathInfo(String s) {
        setPathInfo(s);
        return this;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    @Override
    public String getHeader(String s) {
        return headers.get(s);
    }

    @Override
    public void setContentType(String s) {
        this.contentType = s;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public void setCharacterEncoding(String s) {
        this.characterEncoding = s;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(this.writer);
    }

    public StringWriter getStringWriter() {
        return this.writer;
    }

    @Override
    public void setStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getStatus() {
        return httpStatus;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }
}
