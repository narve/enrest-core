package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.EksAlps;
import no.dv8.microblogging.MBUsers;
import no.dv8.xhtml.generation.elements.body;
import no.dv8.xhtml.generation.elements.h1;
import no.dv8.xhtml.generation.elements.html;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@javax.servlet.annotation.WebServlet(urlPatterns = { "/eks", "/eks/*"})
@Slf4j
public class EksServlet extends HttpServlet {
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
        String title = path;
        EksIndex index = new EksIndex();
        EksForms forms = new EksForms();
        EksQueries queries = new EksQueries();
        MBUsers users= new MBUsers ();

        if( path.equals( "alps" ) ) {
            obj = new XHTMLSerialize<>().generateElement(new EksAlps().eks(), 100 );
            title = "ALPS";
        } else if( path.isEmpty() ) {
            obj = index.index();
        } else if( path.startsWith("forms/")) {
            obj = forms.form( path.substring( "forms/".length()));
        } else if( path.startsWith("form-actions/")) {
            obj = forms.formAction( path.substring( "form-actions/".length()), req);
        } else if( path.startsWith("queriesAsList/")) {
            obj = queries.executeQuery( path.substring( "queriesAsList/".length() ),req );
        } else {
            obj = error404(path);
        }
        o.print( EksHTML.complete(obj, title ).toString());
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
