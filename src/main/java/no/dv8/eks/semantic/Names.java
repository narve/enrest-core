package no.dv8.eks.semantic;

public enum Names {
    description, email, message, name, password, search, user, user_image, website,
    question_text, answer_text,title,
    id
    ;

    @Override
    public String toString() {
        return super.toString().replaceAll("_", "-");
    }

}
