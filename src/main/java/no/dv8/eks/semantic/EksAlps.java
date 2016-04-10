package no.dv8.eks.semantic;

import no.dv8.alps.Alps;
import no.dv8.alps.Descriptor;
import no.dv8.alps.Doc;

import java.util.ArrayList;

import static no.dv8.alps.Descriptor.semantic;

public class EksAlps {

    public Alps eks() {

        Descriptor questionsSearch = Descriptor.builder()
          .doc( new Doc( "Search createForm for questions" ) )
          .id( Types.questions_search.toString() )
          .type( "safe" )
          .rt( "contact" )
          .subs(new ArrayList<>())
          .build()
          .add( semantic( Types.category_search).withDoc( new Doc( "Input for question searches" ) ) );

        return new Alps()
          .withDoc( new Doc( "Eksamensprosjektet :)"))
          .add(
            semantic( Types.person)
            .add( semantic( Types.user_name))
            .add( semantic( Types.fullName))
          ).add( semantic( Types.question ) )
          .add( semantic( Types.questions ) )
          .add( questionsSearch );
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
          .add( item );

        Descriptor collection = Descriptor.builder()
          .id( "collection" )
          .type( "safe" )
          .rt( "contact" )
          .doc( new Doc( "A simple link/createForm for getting a list of contacts" ) )
          .build()
          .add( semantic("nameSearch").withDoc( new Doc( "Input for a search createForm")));


        Alps alps = new Alps();
        alps.getSubs().add( contact );
        alps.getSubs().add( collection );
        alps.setDoc( new Doc( "A contact list"));

        return alps;
    }

}