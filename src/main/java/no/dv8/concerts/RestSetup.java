package no.dv8.concerts;

import no.dv8.rest2.framework.RestContainer;

import java.util.ArrayList;
import java.util.List;

public class RestSetup {

    public static <T> Class typedList( Class<T> clz ) {
        return new ArrayList<T>(){}.getClass();
    }

    public static RestContainer setup(String... args) {

        RestContainer cnt = new RestContainer();

        cnt
          .addRootResource(ConcertList.class, () -> Program.getInstance().getConcerts());

        cnt
          .addRootResourceList(Concert.class, () -> Program.getInstance().getConcerts());

        cnt
          .newTransition(Concert.class, Concert.class)
          .withName("self")
          .action( c -> c )
          .linkFrom( Concert.class )
          .buildAndRegister();

        cnt
          .newTransition(Concert.class, Void.class)
          .withName("delete")
          .delete((Concert m) -> Program.getInstance().remove(m))
          .buildAndRegister();


        cnt
          .newTransition(Concert.class, Concert.class)
          .withName("book-ticket")
          .disallowWhen(m -> !m.isAvailable())
          .delete((Concert m) -> m.setAvailable(false) )
          .buildAndRegister();

        cnt
          .newTransition(Concert.class, List.class)
          .withName("performers")
          .action((Concert m) -> m.getPerformers())
          .buildAndRegister();

        return cnt;
    }



}
