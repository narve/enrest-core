package no.dv8.enrest.handlers;

import no.dv8.enrest.Exchange;

public class MockExchange extends Exchange {

    private String fullPath;

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
}
