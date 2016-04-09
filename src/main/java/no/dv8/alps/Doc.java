package no.dv8.alps;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Doc {
    public String content;
    public String type = "text";

    public Doc(String cnt) {
        this.content = cnt;
    }

    public String toString() {
        return content;
    }

}
