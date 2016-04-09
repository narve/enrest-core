package no.dv8.microblogging;

import no.dv8.eks.semantic.Types;
import no.dv8.eks.semantic.Ids;
import no.dv8.eks.semantic.Rels;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;

import java.util.ArrayList;
import java.util.List;

public class MBUsers {

    public div all() {

        List<Element<?>> userList = new ArrayList<>();
        userList.add(user1Short());
        userList.add(user1Short());

        return
          new div()
            .id(Ids.users)
            .add(new h1(Ids.users.toString()))
            .add(
              new ul()
                .clz(Types.question)
                .add(userList)
            )
            .add(MBIndex.relToA(Rels.first))
            .add(MBIndex.relToA(Rels.next))
            .add(MBIndex.relToA(Rels.previous))
            .add(MBIndex.relToA(Rels.last))
          ;
    }

    li user1Short() {
        String uid = "user1Short";
        return new li()
          .clz(Types.person)
          .add(new span(uid + "text").clz(Types.user_text))
//          .add(new a(uid).rel(Rels.user).href("users/" + uid))
//          .add(new a(Rels.messages_me.toString()).rel(Rels.messages_me).href(Rels.messages_me.toString()))
//
//
//          .add(new span("user1desc").clz(Types.description))
//          .add(new img().clz(Types.user_image).src("images/" + uid))
//          .add(new a("http://user1Short/").rel(Rels.website))
          ;
    }


}
