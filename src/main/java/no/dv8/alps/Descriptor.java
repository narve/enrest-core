package no.dv8.alps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class Descriptor {
    public String id, type, rt;
    public Doc doc;
    public List<Descriptor> subs = new ArrayList<>();
    public Descriptor add( Descriptor sub ) {
        subs.add( sub );
        return this;
    }

    public static Descriptor semantic(Object id) {
        Descriptor d = new Descriptor();
        d.setId( String.valueOf(id) );
        return d;
    }


    public String toString() {
        return id;
    }
}
