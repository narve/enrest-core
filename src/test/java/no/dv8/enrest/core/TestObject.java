package no.dv8.enrest.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestObject {
    String id, stringValue;
    Long boxedLong;
    long primitiveLong;

    NestedTestObject nested;

    public TestObject(String id, String val) {
        this.id = id;
        this.stringValue = val;
    }
}
