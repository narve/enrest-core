package no.dv8.enrest.semantic;

import no.dv8.enrest.alps.Alps;
import no.dv8.enrest.alps.Descriptor;
import no.dv8.enrest.alps.Doc;
import no.dv8.enrest.Exchange;
import no.dv8.xhtml.serializer.XHTMLSerialize;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static no.dv8.enrest.alps.Descriptor.semantic;

public class EksAlps implements Predicate<Exchange>, UnaryOperator<Exchange> {


    public Alps alps() {

        Descriptor questionsSearch = Descriptor.builder()
          .doc(new Doc("Search getForm for questions"))
          .id(Types.questions_search.toString())
          .type("safe")
          .rt("contact")
          .subs(new ArrayList<>())
          .build()
          .add(semantic(Types.category_search).withDoc(new Doc("Input for question searches")));

        return new Alps()
          .withDoc(new Doc("Eksamensprosjektet :)"))
          .add(
            semantic(Types.person)
              .add(semantic(Types.user_name))
              .add(semantic(Types.fullName))
          ).add(semantic(Types.question))
          .add(semantic(Types.questions))
          .add(questionsSearch);
    }


    public Alps sample() {

        Descriptor item = Descriptor.builder()
          .id("item")
          .type("safe")
          .doc(new Doc("A link to an individual contact"))
          .build();

        Descriptor contact = semantic("contact")
          .add(semantic("item"))
          .add(semantic("fullName"))
          .add(semantic("phone"))
          .add(item);

        Descriptor collection = Descriptor.builder()
          .id("collection")
          .type("safe")
          .rt("contact")
          .doc(new Doc("A simple link/getForm for getting a list of contacts"))
          .build()
          .add(semantic("nameSearch").withDoc(new Doc("Input for a search getForm")));


        Alps alps = new Alps();
        alps.getSubs().add(contact);
        alps.getSubs().add(collection);
        alps.setDoc(new Doc("A contact list"));

        return alps;
    }

    @Override
    public boolean test(Exchange x) {
        return x.getFullPath().endsWith("/alps");
    }

    @Override
    public Exchange apply(Exchange exchange) {
        return exchange.withOutEntity(new XHTMLSerialize<>().generateElement(new EksAlps().alps(), 100));
    }
}
