package no.dv8.enrest.spi;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public interface Outputter {

    void output( Object o, ServletResponse out ) throws IOException;
}
