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

import static no.dv8.eks.rest.EksQueries.pathToQueries;
import static no.dv8.eks.rest.EksQueries.pathToQueryResult;
import static no.dv8.enrest.creators.FormHelper.pathToCreateResult;
import static no.dv8.enrest.creators.FormHelper.pathToCreators;

@javax.servlet.annotation.WebServlet(urlPatterns = { "/eks", "/eks/*"})
@Slf4j
public class EksServlet extends HttpServlet {

    public static String basePath = "/eks/";

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
        } else if( path.startsWith(pathToQueries + "/")) {
            obj = queries.searchForm( path.substring( pathToQueries.length()+1));
        } else if( path.startsWith(pathToQueryResult+"/")) {
            obj = queries.executeQuery( path.substring( pathToQueryResult.length() +1),req );
        } else if( path.startsWith(pathToCreators+"/")) {
            obj = forms.createForm( path.substring( pathToCreators.length()+1));
        } else if( path.startsWith(pathToCreateResult+"/")) {
            obj = forms.executeForm( path.substring( pathToCreateResult.length()+1), req);
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
