package no.dv8.eks.semantic;

public enum Ids {

    queries,
    questions,
    users;

    @Override
    public String toString() {
        return super.toString().replaceAll("_", "-");
    }
}
