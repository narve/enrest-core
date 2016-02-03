package no.dv8.concerts;

import no.dv8.rest2.framework.Link;
import no.dv8.rest2.framework.RestContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class RestSetupTest {

    static RestContainer cnt;

    @BeforeClass
    public static void init() {
        cnt = RestSetup.setup();
    }

    @Test
    public void testRoots() {
        List links = cnt()
          .linksFrom(Void.class);


        assertThat("links linkedFrom " + Void.class + ": " + links, links.size(), equalTo(1));
    }

    private RestContainer cnt() {
        return cnt;
    }

    @Test
    public void testOne() {
        List links = cnt()
          .linksFrom(ConcertList.class);


        assertThat( "links linkedFrom " + ConcertList.class + ": " + links, links.size(), equalTo( 0 ) );
    }

    @Test
    public void testZero() {
        List links = cnt()
          .linksFrom(Performer.class);

        assertThat( "links linkedFrom " + Performer.class + ": " + links, links.size(), equalTo( 0 ) );
    }

    @Test
    public void testSelf() {
        List links = cnt()
          .linksFrom(Concert.class)
          .stream()
          .filter( t -> t.getName().equals("self"))
          .collect(toList());

        assertThat( "links linkedFrom " + Concert.class + ": " + links, links.size(), equalTo( 1 ) );
    }

}