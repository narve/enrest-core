package no.dv8.eks.testutil;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Slf4j
public class IntegrationTestUtil {

    static EntityManager em;
    static EntityManagerFactory emf;

    public static EntityManager em() {
        if (em == null) {
            try {
                em = emf().createEntityManager();
            } catch (Exception e) {
                Throwable t = e;
                while ((t = t.getCause()) != null) {
                    t.printStackTrace();
                }
                throw e;
            }
        }
        return em;
    }

    static EntityManagerFactory emf() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("Eks");
        }
        return emf;
    }

    public static void setup(Object theTestCase) {
        List<String> toDisable = Arrays.asList( "org.hibernate", "java.sql.DatabaseMetaData");
        toDisable.forEach( l -> {
              Logger logger = (Logger) LoggerFactory.getLogger("org.hibernate");
              logger.setLevel(Level.OFF);
          } );

//            ClockMock.init();
//            Logging.sleep = false;
        wrapClassLoader();
        em().getTransaction().begin();
        Injector injector = Guice.createInjector(new IntegrationTestModule());
        injector.injectMembers(theTestCase);
    }

    public static void finishEJBMethod() {
        em().flush();
        em().getTransaction().commit();
//        TestTransSyncReg.getInstance().afterCompletion(Status.STATUS_COMMITTED);
        em().getTransaction().begin();
    }

    public static void teardown() {
        if (em != null) {
            em.close();
            emf.close();
        }
        em = null;
        emf = null;
    }

//    public static Archive<?> standardArchive() {
//        return ShrinkWrap.create(WebArchive.class, "test.war").
//                addClasses(Resources.class).
//                addClasses(WebConsts.class).
//                addClasses(Chars.class, Lists.class).
//                //                addPackage(PlayerController.class.getPackage()).
//                        //                addClasses(PlayerController.class, GameController.class, LanguagePackController.class).
//                        addPackage(class
//                        .getPackage()).
//                //                addPackage(GameListEntry.class.getPackage()).
//                        addAsLibraries(MavenArtifactResolver.resolve("org.jdom:jdom:1.1")).
//                addAsResource("META-INF/persistence-aero.xml", "META-INF/persistence.xml").
//                addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
//
//    }

    public static String debug(Response resp) {
        List<Object> l = new ArrayList<>();
        l.add(resp.getStatus());
        l.add(resp.getEntity());
        for (Iterator<Map.Entry<String, List<Object>>> it = resp.getMetadata().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, List<Object>> me = it.next();
            l.add(me.getKey());
            l.add(me.getValue());
        }
        return debug(l);
    }

    public static String debug(List<Object> oa) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < oa.size(); i++) {
            sb.append("Object[").append(i).append("]: ").append(oa.get(i) == null ? "NULL" : debugObject(oa.get(i))).append("; ");
        }
        return sb.toString();
    }

    public static String debugObject(Object object) {
        return object.getClass() + " " + object.toString();
    }

    public static void wrapClassLoader() {

        List<PersistenceProvider> persistenceProviders = PersistenceProviderResolverHolder
                .getPersistenceProviderResolver()
                .getPersistenceProviders();


//        if( true ) throw new RuntimeException( persistenceProviders.toString() );

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
                new ClassLoader(current) {
                    @Override
                    public Enumeration<URL> getResources(String name) throws IOException {
                        if ("META-INF/persistence.xml".equalsIgnoreCase(name)) {
                            log.debug( "Asked for {} - overriding!", name);
                            return super.getResources("META-INF/persistence-test.xml");
                        } else {
                            return super.getResources(name);
                        }
                    }
                }
        );
    }


}
