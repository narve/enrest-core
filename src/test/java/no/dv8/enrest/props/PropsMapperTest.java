package no.dv8.enrest.props;

import no.dv8.enrest.core.NestedTestObject;
import no.dv8.enrest.core.TestObject;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PropsMapperTest {

    @Test
    public void testKeysAndValues() {
        TestObject to = new TestObject("id", "string");
        to.setBoxedLong(1L);
        to.setPrimitiveLong(2L);
        List<PropsMapper.PropNode> props = new PropsMapper().props(to, true);
        List<String> keys = props.stream().map(p -> p.getName()).collect(toList());
        assertThat(keys.size(), equalTo(6));

        assertTrue(keys.contains("id"));
        assertTrue(keys.contains("stringValue"));
        assertThat(keys, not(hasItem("nested")));

        Object stringPN = props.stream().filter(pn -> pn.getName().equals("stringValue")).findFirst().get().getVal();
        assertThat(stringPN, equalTo("string"));
    }

    @Test
    public void testKeysWithMissings() {
        TestObject to = new TestObject("id", "string");
        List<PropsMapper.PropNode> props = new PropsMapper().props(to, false);
        List<String> keys = props.stream().map(p -> p.getName()).collect(toList());
        assertThat(keys, contains("id", "primitiveLong", "stringValue"));
    }

    @Test
    public void testNestedKeysAndValues() {
        TestObject to = new TestObject("id", "string");
        NestedTestObject nested = new NestedTestObject();
        nested.setId(123);
        nested.setNestedPrimitiveLong(1234);
        to.setNested(nested);
        List<PropsMapper.PropNode> props = new PropsMapper().props(to, true);
        List<String> keys = props.stream().map(p -> p.getName()).collect(toList());

        assertThat(keys, contains("boxedLong", "id", "nested.id", "nested.nestedPrimitiveLong", "primitiveLong", "stringValue"));

        Optional<PropsMapper.PropNode> first = props.stream().filter(pn -> pn.getName().equals("nested.nestedPrimitiveLong")).findFirst();
        assertThat(first.get().getVal(), equalTo(1234L));
    }

}