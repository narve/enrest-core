package no.dv8.rest2.framework;

import no.dv8.concerts.Concert;
import no.dv8.concerts.Program;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RestContainerTest {

    @Test
    public void testIt() {

        Program program = new Program();

//        r.autoget( Concert.class, id -> program.getConcert(id));

        Rest2 r = new Rest2();
        r
          .get(Concert.class, "enrest:concert")
          .urlpattern("/concert/:id", t -> t.getId())
          .action(request -> program.getConcert(request.getUriParam("id")));


//        autopost( Concert.class, concert -> program.insert( program );
        r
          .post(Concert.class, "enrest-concert")
          .urlpattern("/concert/:id", t -> t.getId())
          .action(request -> program.insert(fromJson(Concert.class, request.body())));

        r.root(request -> program.getConcerts());

//
//
//
//        register("enreset:concert", Concert.class);
//
//        operation()
//          .httpMethod("post")
//          .
//

        /*

        String concert = "concert", concertCollection = "concert-collection";

        RestContainer cnt = new RestContainer();

        cnt.register(request -> new Program().getConcerts());

        cnt.transition(req).action()


        cnt
          .newTransition(Void.class, List.class)
          .action(v -> new Program().getConcerts())
          .buildAndRegister();

        cnt
          .newTransition(Void.class, Concert.class)
          .action(v -> new Program().getConcerts())
          .buildAndRegister();


        */

          assertThat(false, equalTo(false));
    }

    private <T> T fromJson(Class<T> clz, String body) {
        return null;
    }

    public static class URIProvider {

    }

    public static class Rest2Builder<T> {
        public Rest2Builder<T> urlpattern(String p, Function<T, String>... funcs) {
            return this;
        }

        public Rest2Builder<T> action(Function<Request, T> func) {
            return this;
        }
    }

    public static Function<Object, String> defaultURIProvider() {
        return o -> {
            Objects.requireNonNull(o, "can't get URI for NULL object!");
            try {
                return o.getClass().getMethod("getId").invoke(o).toString();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Unable to get id for object of class [" + o.getClass().getName() + "]", e );
            }
        };
    }

    public static class Rest2 {

        Function<Object, String> uriProvider = defaultURIProvider();


        public <T> Rest2Builder<T> get(Class<T> clz, String uri) {
            return new Rest2Builder<>();
        }

        public <T> Rest2Builder<T> post(Class<T> clz, String uri) {
            return new Rest2Builder<>();
        }

        public <T> Rest2 root(Function<Request, T> supplier) {
            return this;
        }
    }

}