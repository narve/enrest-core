package no.dv8.enrest.semantic;

public enum Types {
//    all, date_time, description, friends, followers, me, mentions, message, message_post, message_reply, message_share, single, messages_search, message_text,
//    search, shares, person, user_add, user_follow, user_image, user_name, user_text, user_update, users_search,

    edit,

    question, user_text, question_set, person,

    user_image, description, message_post, user_add, users_search, messages_search,

    questions, questions_search, question_add,
    user_name, fullName, category_search;



    @Override
    public String toString() {
        return super.toString().replaceAll("_", "-");
    }
}
