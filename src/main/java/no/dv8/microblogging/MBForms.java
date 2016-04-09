package no.dv8.microblogging;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.semantic.Types;
import no.dv8.eks.semantic.Names;
import no.dv8.xhtml.generation.elements.form;
import no.dv8.xhtml.generation.elements.input;
import no.dv8.xhtml.generation.elements.legend;
import no.dv8.xhtml.generation.elements.textarea;

@Slf4j
public class MBForms {

    form messagePostForm() {
        return new form()
          .clz(Types.message_post)
          .post()
          .add(new legend(Types.message_post.toString()))
          .add(new textarea().name(Names.message))
          .add(new input().submit().value(Types.message_post));
    }

    form userAddForm() {
        return new form()
          .clz(Types.user_add)
          .post()
          .add(new legend(Types.user_add.toString()))
          .add(new input().text().name(Names.user))
          .add(new input().text().name(Names.email))
          .add(new input().password().name(Names.password))

          .add(new textarea().name(Names.description))
          .add(new input().file().name(Names.user_image.toString()))
          .add(new input().text().name(Names.website.toString()))
          .add(new input().submit().value(Types.user_add));
    }


    public form form(String name) {
        String clzName = name.replaceAll( "\\-", "\\_" );
        log.info( "Clzname: {}", clzName );
        Types clz = Types.valueOf(clzName);
        switch (clz) {
            case users_search:
            case messages_search:
                return searchForm(clz);
            case user_add:
                return userAddForm();
            case message_post:
                return messagePostForm();
            default:
                return null;
        }
    }

    public form searchForm(Types rel) {
        return new form()
          .clz(rel)
          .get()
          .add(new legend(rel.toString()))
          .add(new input().text().name(Names.search))
          .add(new input().submit().value(rel));
    }
//
//    form usersSearchForm() {
//        return new form()
//          .clz(Types.users_search)
//          .get()
//          .add(new legend(Types.users_search.toString()))
//          .add(new input().text().name(Names.search))
//          .add(new input().submit().value(Types.users_search));
//    }
//
//
//    form messagesSearchForm() {
//        return new form()
//          .clz(Types.messages_search)
//          .get()
//          .add(new legend(Types.messages_search.toString()))
//          .add(new input().text().name(Names.search))
//          .add(new input().submit().value(Types.messages_search));
//    }


}
