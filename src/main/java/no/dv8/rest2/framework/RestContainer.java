package no.dv8.rest2.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class RestContainer {

//    private static RestContainer instance = new RestContainer();
//    public static RestContainer getRestContainer() {
//        return instance;
//    }

    List<Transition> transitions = new ArrayList<>();
    private int counter = 0;

    public <F, T> TransitionBuilder<F, T> newTransition(Class<F> from, Class<T> to) {
        return new TransitionBuilder(this, from, to);
    }

    public <T> void addRootResource( Class<T> clz, Supplier<T> f ) {
//        root.add(f);
        Transition<Void, T> r = new TransitionBuilder<Void, T>(this, Void.class, clz )
          .withName( clz.getSimpleName() )
          .action( asdf -> f.get() )
          .buildAndRegister();
    }

    public <T> void addRootResourceList( Class<T> clz, Supplier<List<T>> f ) {
//        root.add(f);
        Transition<Void, List> r = new TransitionBuilder<Void, List>(this, Void.class, List.class )
          .withName( clz.getSimpleName() + "Collection" )
          .action( asdf -> f.get() )
          .buildAndRegister();
    }

    public List<Transition> linksFrom(Class res) {
        return transitions
          .stream()
          .filter(t -> t.linkedFrom.contains(res) || res.equals(t.from))
          .collect(toList());
   }

    public List<Transition> transitions() {
        return transitions;
    }

    public String newUrl() {
        return "/rest/" + counter++;
    }

    public  <T> RestContainer register(Function<Request, T> action) {
        return this;
    }
}
