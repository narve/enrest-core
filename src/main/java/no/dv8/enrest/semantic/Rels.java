package no.dv8.enrest.semantic;

public enum Rels {
    self, index, first, last, next, previous, profile, edit,

// message, message_post, message_reply, message_share, messages_all, messages_friends,
//    messages_me, messages_mentions, messages_shares, messages_search,
//    creator, user_follow, user_me, user_update, users_all, users_friends, users_followers,website;
 users_search,user_add,
    questions_search, question_add,
    delete_form;
    @Override
    public String toString() {
        return super.toString().replaceAll("_", "-");
    }
}
