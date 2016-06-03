package no.dv8.utils;

import org.junit.Test;

import java.util.function.UnaryOperator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class FuncListTest {
    @Test
    public void allShouldEvaluteLazily() throws Exception {

        StringBuilder sb = new StringBuilder();

        UnaryOperator<Object> all = new FuncList<Object>()
          .add("setter", o -> true, o -> "something")
          .add("conditional", o -> o != null, o -> sb.append("conditional"))
          .all();

        all.apply(null );
        assertThat( sb.toString(), equalTo( "conditional" ));
    }

    @Test
    public void testTestSetup() throws Exception {

        StringBuilder sb = new StringBuilder();

        UnaryOperator<Object> all = new FuncList<Object>()
          .add("setter", o -> true, o -> o)
          .add("conditional", o -> o != null, o -> sb.append("conditional"))
          .all();

        all.apply("theobject" );

        assertThat( sb.toString(), equalTo( "conditional" ));

    }

}