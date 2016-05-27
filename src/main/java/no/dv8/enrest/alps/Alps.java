package no.dv8.enrest.alps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.List;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class Alps {

    public String toString() {
        return doc.toString();
    }

    public Alps add( Descriptor sub ) {
        subs.add( sub );
        return this;
    }
    String version = "1.0";
    Doc doc;
    List<Descriptor> subs = new ArrayList<>();

}
