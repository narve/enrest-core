package no.dv8.eks.rest;

import static no.dv8.enrest.resources.FormHelper.pathToCreateResult;
import static no.dv8.enrest.resources.FormHelper.pathToCreators;

public class ResourcePaths {
    private final String pathToResource = "view-resource";
    private final String editPathToResource = "edit-resource";
    private final String pathToQueries = "queries";
    private final String pathToQueryResult = "query-result";

    public String viewItem( String type, String id ) {
        return pathToResource + "/" + type + "/" + id;
    }

    public String editItem(String type, String id) {
        return editPathToResource + "/" + type + "/" + id;
    }

    public boolean isItem(String path ) {
        return path.startsWith(pathToResource + "/" );
    }

    public String type(String path) {
//        String sub = path.substring(pathToResource.length() + 1);
//        String itemClass = sub.split("/")[0];
//        return itemClass;
        return path.split( "/" )[1];
    }

    public String id(String path) {
        String sub = path.substring(pathToResource.length() + 1);
        String s = sub.split("/")[1];
        return s;
    }

    public boolean isEditForm(String path) {
        return path.startsWith(editPathToResource + "/" );
    }

    public boolean isQueryForm(String path) {
        return path.startsWith(pathToQueries + "/");
    }

    public String queryName(String path) {
        return path.split( "/" )[1];
    }

    public boolean isQueryResult(String path) {
        return path.startsWith(pathToQueryResult + "/");
    }

    public boolean isCreateForm(String path) {
        return path.startsWith(pathToCreators + "/");
    }

    public boolean isCreateResult(String path) {
        return path.startsWith(pathToCreateResult + "/");
    }

    public String createForm(String name) {
        return pathToCreators + "/" + name;
    }

    public String query(String rel) {
        return pathToQueries + "/" + rel;
    }

    public Object queryResult(Object rel) {
        return pathToQueryResult + "/" + rel;
    }
}
