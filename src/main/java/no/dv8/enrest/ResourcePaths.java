package no.dv8.enrest;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourcePaths {

    private final String pathToResource = "";
    private final String editPathToResource = "edit-resource";
    private final String deletePathToResource = "delete-resource";
    private final String pathToDeleteResult = "delete-result";
    private final String pathToQueries = "queries";
    private final String pathToQueryResult = "query-result";

    private final String pathToCreators = "create-forms";
    private final String pathToCreateResult = "create-result";

    private final String basePath;

    public ResourcePaths(String basePath) {
        if( !basePath.startsWith("/")) throw new IllegalArgumentException("basepath should start with /");
        if( !basePath.endsWith("/")) throw new IllegalArgumentException("basepath should end with /");
        this.basePath = basePath;
    }

    public String full( String partial ) {
        while( partial.startsWith("/" ) ) partial = partial.substring(1);
        return basePath + partial;
    }

    public String pure( final String full ) {
        String path = full;
        if (!path.startsWith("/")) path = "/" + path;
        if (!path.endsWith("/")) path += "/";
        String s = path.substring( basePath.length());
        while( s.startsWith("/" ) ) s = s.substring(1);
        while( s.endsWith("/" ) ) s = s.substring(0, s.length()-1);
        return s;
    }

    public String type(String path) {
        if( isItem(path)) {
            return pure( path ).split( "/" )[0];
        } else {
            return pure(path).split("/")[1];
        }
    }

    public String id(String path) {
        if( isItem(path)) {
            return pure( path ).split( "/" )[1];
        } else {
            return pure(path).split("/")[2];
        }
    }

    public boolean isItem(String path ) {
//        return pure( path ).startsWith(pathToResource + "/" );
//        if( isQ)
        if( isQueryResult(path)) {
            return false;
        }
        String p = pure(path);
        Matcher m = Pattern.compile( pathToResource + "([^/]+)/([^/])+" ).matcher(p);
        if( m.matches() ) {
            return true;
        }
        return false;
    }

    public boolean isEditForm(String path) {
        return pure( path ).startsWith(editPathToResource + "/" );
    }

    public boolean isDeleteForm(String path) {
        return pure( path ).startsWith(deletePathToResource + "/" );
    }

    public boolean isQueryForm(String path) {
        return pure( path ).startsWith(pathToQueries + "/");
    }

    public boolean isDeleteFormResult(String path) {
        return pure(path).startsWith(pathToDeleteResult);
    }

    public String queryName(String path) {
        return pure( path ).split( "/" )[1];
    }

    public boolean isQueryResult(String path) {
        return pure( path ).startsWith(pathToQueryResult + "/");
    }

    public boolean isCreateForm(String path) {
        return pure( path ).startsWith(pathToCreators + "/");
    }

    public boolean isCreateResult(String path) {
        return pure( path ).startsWith(pathToCreateResult + "/");
    }

    public boolean isRoot( String path ) {
        String s = pure(path);
        return pure(path).isEmpty();
    }

    public String viewItem(String type, String id ) {
        return full( pathToResource + "/" + type + "/" + id );
    }

    public String editForm(String type, String id) {
        return full( editPathToResource + "/" + type + "/" + id );
    }

    public String deleteForm(String type, String id) {
        return full( deletePathToResource + "/" + type + "/" + id );
    }

    public String createForm(String name) {
        return full( pathToCreators + "/" + name);
    }

    public String query(String rel) {
        return full( pathToQueries + "/" + rel);
    }

    public String queryResult(Object rel) {
        return full( pathToQueryResult + "/" + rel);
    }

    public String createAction(Object name) {
        return full( pathToCreateResult+"/" + name);
    }

    public String root() {
        return basePath;
    }

    public String deleteFormResult(String simpleName, String id) {
        return full( pathToDeleteResult + "/" + simpleName + "/" + id );
    }

}
