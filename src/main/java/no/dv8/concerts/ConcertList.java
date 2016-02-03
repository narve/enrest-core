package no.dv8.concerts;

import java.util.ArrayList;
import java.util.List;

public class ConcertList extends ArrayList<Concert> {
    public ConcertList(List<Concert> concerts) {
        super(concerts);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
