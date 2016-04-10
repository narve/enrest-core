package no.dv8.eks.controllers;

import lombok.extern.slf4j.Slf4j;
import no.dv8.eks.model.User;
import no.dv8.eks.testutil.IntegrationTestUtil;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

//@RunWith(WeldJUnit4Runner.class)
@Slf4j
public class UsersJPATest {

    @Inject
    UsersJPA users;

    @After
    public void tearDown() {
        log.debug( "Tearing down");
        IntegrationTestUtil.teardown();
    }

    @Before
    public void setup() {
        IntegrationTestUtil.setup(this);
        log.info( "Setting up test" );
    }

    @Test
    public void aaaaemptyList() throws Exception {
        assertThat( users.all(), empty() );
    }

    @Test
    public void insertOneAndList() throws Exception {
        assertThat( users.all(), empty() );
        User u = new User("1narve sætre", "1narve@dv8.no");
        users.insert(u);
        IntegrationTestUtil.finishEJBMethod();
        assertThat( users.all(), Matchers.contains( u ) );
    }


    @Test
    public void emptyList2() throws Exception {
        assertThat( users.all(), empty() );
    }


    @Test
    public void insertTwoAndList() throws Exception {
        assertThat( users.all(), empty() );
        User u1 = new User("1narve sætre1", "1narve@dv8.no1");
        users.insert(u1);
        assertThat( users.all(), Matchers.contains( u1 ) );

        User u2 = new User("2narve sætre2", "2narve@dv8.no2");
        users.insert(u2);
        assertThat( users.all(), Matchers.contains( u1, u2 ) );

        IntegrationTestUtil.finishEJBMethod();
    }

    @Test
    public void ZZZemptyList2() throws Exception {
        assertThat( users.all(), empty() );
    }


}