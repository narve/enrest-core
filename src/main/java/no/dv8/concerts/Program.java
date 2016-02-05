package no.dv8.concerts;

import java.util.*;

import static java.util.Arrays.asList;

public class Program {

    ConcertList concerts = initConcerts();

    ConcertList initConcerts() {
        ConcertList cl = new ConcertList(asList(new Concert("Muse Drone", true, 101), new Concert("Nick Cave", false, 102)));
        cl.get(0).getPerformers().add( new Performer("Muse", "UK"));
        cl.get(0).getPerformers().add( new Performer("AHA", "SE"));
        return cl;
    }

    public ConcertList getConcerts() {
        return concerts;
    }

    public List<Concert> getConcerts2() {
        return new ArrayList<>( getConcerts() );
    }

    private static Program instance = new Program();
    public static Program getInstance() {
        return instance;
    }

    public void remove(Concert m) {
        concerts.remove(m);
    }

    public Concert getConcert(String id) {
        Objects.requireNonNull(id, "Required: id");
        Optional<Concert> first = concerts.stream().filter(c -> id.equals(c.getId())).findFirst();
        if( first.isPresent() ) return first.get();
        else throw new NoSuchElementException("No concert with id '" + id + "'");
    }

    public Concert insert(Concert concert) {
        concerts.add( concert);
        return concert;
    }

    public Concert addConcert(Concert x) {
        Objects.requireNonNull(x, "Can insert null");
        if( x.getId() == null )
            x.setId( UUID.randomUUID().toString() );

        concerts.add(x);
        return x;
    }
}
