package no.dv8.eks.rest;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.EksAlps;
import no.dv8.xhtml.generation.elements.body;
import no.dv8.xhtml.generation.elements.h1;
import no.dv8.xhtml.generation.elements.html;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static no.dv8.eks.rest.EksQueries.pathToQueries;
import static no.dv8.eks.rest.EksQueries.pathToQueryResult;
import static no.dv8.eks.rest.EksResources.editPathToResource;
import static no.dv8.eks.rest.EksResources.pathToResource;
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
//        ServletOutputStream o = res.getOutputStream();
        PrintWriter writer = res.getWriter();
        res.setContentType( "text/html");

        writer.write( "<!DOCTYPE html>\n");

        String path = req.getPathTranslated() == null ? "" : req.getPathInfo();
        if( path.startsWith("/")) path = path.substring(1);
        log.info( "PATH: {}", path );

        Element<?> obj;
        String title = path;
        EksIndex index = new EksIndex();
        EksForms forms = new EksForms();
        EksQueries queries = new EksQueries();
        EksResources resources = new EksResources();

        String method = req.getMethod();

        if( path.equals( "alps" ) ) {
            obj = new XHTMLSerialize<>().generateElement(new EksAlps().eks(), 100 );
            title = "ALPS";
        } else if( path.isEmpty() ) {
            obj = index.index();
        } else if( path.startsWith(pathToResource + "/" ) && method.equalsIgnoreCase("GET")) {
            obj = resources.itemToElement( path.substring( pathToResource.length()+1));
        } else if( path.startsWith(pathToResource + "/" ) && (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("put"))) {
            obj = resources.executeUpdate( path.substring( pathToResource.length()+1), req);
        } else if( path.startsWith(editPathToResource + "/")) {
            obj = forms.editForm( path.substring( editPathToResource.length()+1));
        } else if( path.startsWith(pathToQueries + "/")) {
            obj = queries.searchForm( path.substring( pathToQueries.length()+1));
        } else if( path.startsWith(pathToQueryResult+"/")) {
            obj = queries.executeQuery( path.substring( pathToQueryResult.length() +1),req );
        } else if( path.startsWith(pathToCreators+"/")) {
            obj = forms.createForm( path.substring( pathToCreators.length()+1));
        } else if( path.startsWith(pathToCreateResult+"/")) {
            obj = forms.executeCreate( path.substring( pathToCreateResult.length()+1), req);
        } else {
            obj = error404(path);
        }
        res.setCharacterEncoding("utf-8");

        writer.print( EksHTML.complete(obj, title ).toString());
        writer.close();
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
