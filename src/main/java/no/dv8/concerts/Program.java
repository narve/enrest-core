package no.dv8.concerts;

import java.util.*;

import static java.util.Arrays.asList;

public class Program {

    private ConcertList concerts = new ConcertList(asList(new Concert("Terminator 1", true, 101), new Concert("Terminator 2", false, 102)));

    public ConcertList getConcerts() {
        return concerts;
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
