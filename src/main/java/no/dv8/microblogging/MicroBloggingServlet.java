package no.dv8.microblogging;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.Rels;
import no.dv8.xhtml.generation.elements.body;
import no.dv8.xhtml.generation.elements.h1;
import no.dv8.xhtml.generation.elements.html;
import no.dv8.xhtml.generation.support.Element;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@javax.servlet.annotation.WebServlet(urlPatterns = { "/mb", "/mb/*"})
@Slf4j
public class MicroBloggingServlet extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        ServletOutputStream o = res.getOutputStream();

        String path = req.getPathTranslated() == null ? "" : req.getPathInfo();
        if( path.startsWith("/")) path = path.substring(1);
        log.info( "PATH: {}", path );

        Element<?> obj;
        MBIndex index = new MBIndex();
        MBForms forms = new MBForms();
        MBUsers users= new MBUsers ();

        if( path.isEmpty() ) {
            obj = index.index();
        } else if( path.equals( "Rels.users_all".toString())) {
            obj = users.all();
        } else if( path.startsWith("forms")) {
            obj = forms.form( path.substring( "forms/".length()));
        } else {
            obj = error404(path);
        }
        o.print( MBIndex.complete(obj ).toString());
        o.close();
    }

    static html error404(String path) {
        return new html().add( new body().add( new h1( "404: " + path)));
    }


    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
