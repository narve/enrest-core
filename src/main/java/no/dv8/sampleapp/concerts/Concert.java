package no.dv8.sampleapp.concerts;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Concert {

    String id;
    String name;
    List<Performer> performers = new ArrayList<>();
    boolean available;

    public Concert(String s, boolean a, int i) {
        name = s;
        available = a;
        id = ""+i;
    }

    public String toString() {
        return name;
    }

}
