package no.dv8.enrest.props;

import no.dv8.enrest.core.NestedTestObject;
import no.dv8.enrest.core.TestObject;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class PropsMapperTest {

    @Test
    public void testMap() {
        TestObject to = new TestObject("id", "string" );
        to.setBoxedLong(1L);
        to.setNested( new NestedTestObject());
        to.setPrimitiveLong(2L);
        Map<String, String> map = new PropsMapper().toMap(to, "");

        assertThat( map.size(), equalTo( 5 ) );

        assertTrue( map.containsKey("id" ) );
        assertThat( map.keySet(), hasItem("nested" ) );
        assertThat( map.get("nested"), equalTo( new NestedTestObject().toString()) );
    }

    @Test
    public void testMapWithMissings() {
        TestObject to = new TestObject("id", "string" );
        Map<String, String> map = new PropsMapper().toMap(to, "");

        assertThat( map.keySet(), contains("id", "primitiveLong", "stringValue") );
    }

    @Test
    public void testNesting() {
        TestObject to = new TestObject("id", "string" );
        NestedTestObject nested = new NestedTestObject();
        nested.setId(123);
        nested.setNestedPrimitiveLong(1234);
        to.setNested( nested);
        Map<String, String> map = new PropsMapper().toMap(to, "");

        assertThat( map.keySet(), contains("id", "nested.id", "nested.nestedPrimitiveLong", "primitiveLong", "stringValue") );

    }

}