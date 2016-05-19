package no.dv8.enrest.core;

import no.dv8.enrest.resources.Resource;
import no.dv8.xhtml.generation.support.Element;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class CreatorTest {

    public Resource<TestObject> resource() {
        return new TestObjectResource();
    }


    @Test
    public void testCreation() {
        TestObject t = new TestObject();
        t.setNested(new NestedTestObject());
        String val1 = "zxcv";
        Long val2 = 654987L;
        t.getNested().setNestedPrimitiveLong(val2);
        t.setStringValue("zxcv");
        List<Element<?>> inputs = resource().creator().inputs(t);
        assertThat( inputs.size(), equalTo(6));

        String reduced = inputs.stream().map(e -> e.toString()).reduce("", (a, b) -> a + b);

        assertThat( reduced, containsString( "input" ));
        assertThat( reduced, containsString( "value='" + val1 + "'" ));
        assertThat( reduced, containsString( "value='" + val2 + "'" ));
    }

}
