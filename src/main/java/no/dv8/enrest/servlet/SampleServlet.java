package no.dv8.enrest.servlet;

import no.dv8.eks.rest.EksApi;
import no.dv8.eks.rest.EksIndex;
import no.dv8.enrest.EnrestResource;
import no.dv8.enrest.container.Enrest;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/enrest/*")
public class SampleServlet extends EnrestServletBase {
//    @Override
    public Enrest getEnrest() {
        Enrest r = new Enrest();
//        r.getResources().addAll( EksApi.resources());
        return r;
    }

//    @Override
    public String getRootPath() {
        return "/enrest/";
    }
}
