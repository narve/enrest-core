package no.dv8.eks.rest;

public class ResourcePaths {
    private final String pathToResource = "view-resource";
    private final String editPathToResource = "edit-resource";
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
        return pure( path ).split( "/" )[1];
    }

    public String id(String path) {
        String s = pure( path ).split("/")[2];
        return s;
    }

    public boolean isItem(String path ) {
        return pure( path ).startsWith(pathToResource + "/" );
    }

    public boolean isEditForm(String path) {
        return pure( path ).startsWith(editPathToResource + "/" );
    }

    public boolean isQueryForm(String path) {
        return pure( path ).startsWith(pathToQueries + "/");
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

    public String editItem(String type, String id) {
        return full( editPathToResource + "/" + type + "/" + id );
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
}
