package no.dv8.enrest.semantic;

public enum Ids {

    queries,
    questions,
    users;

    @Override
    public String toString() {
        return super.toString().replaceAll("_", "-");
    }
}
