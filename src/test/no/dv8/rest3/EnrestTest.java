package no.dv8.rest3;

import no.dv8.concerts.Concert;
import no.dv8.concerts.Program;
import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class EnrestTest {

    @Test
    public void test() {

        Program p = new Program();
        Enrest r = new Enrest();
        r.single(String.class, Concert.class)
          .method( "GET")
          .handler( id -> asList( p.getConcert(id)))
          .buildAndRegister();

        r.collection(Void.class, Concert.class)
          .method( "GET")
          .handler( (x) -> p.getConcerts() )
          .buildAndRegister();


        System.out.println(r.index(r::form).toString());
//        assertThat( r.list().size(), equalTo( 1 ) );

    }

}