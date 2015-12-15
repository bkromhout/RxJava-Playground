package bkromhout.rxjava;

import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Examples here are slightly more involved.
 */
public class Playground2 {
    /**
     * Say this is a list of URLs.
     */
    ArrayList<String> urls = new ArrayList<>(Arrays.asList("url1", "url2", "url3", "url4"));
    /**
     * Say we have this observable that returns a list of URLs.
     */
    Observable<List<String>> query = Observable.just(urls);

    /**
     * Do work.
     */
    public void run() {
        test1();
        test2();
        test3();
        test4();
    }

    /**
     * Maybe we want to print the urls from query... how could we do it? This is the worst way.
     */
    private void test1() {
        System.out.println("Playground2 1.");
        // This is clearly the worst. It may not seem bad, but note that you now have a loop in your subscriber, and
        // even worse, in order to transform the data stream you'd now have to do it in the subscriber!
        query.subscribe(urls -> {
            for (String url : urls) System.out.println(url);
        });
        // So, this works, but it's very bad. Map isn't great here either, because you'd still have a loop inside of it.
    }

    /**
     * So, perhaps we can use .from()? It's better, but not great.
     */
    private void test2() {
        System.out.println("Playground2 2.");
        // So what about .from()? Will that help us? Well, we can't use .from() directly on the Observable (query) so...
        query.subscribe(urls -> {
            Observable.from(urls)
                      .subscribe(System.out::println);
        });
        // Well that's gross, we now have nested subscribers. It's hard to modify, looks ugly, and breaks a number of
        // RxJava features.
    }

    /**
     * Enter .flatmap(), which is the bees knees, in its verbose form.
     */
    private void test3() {
        System.out.println("Playground2 3.");
        // Flatmap takes emissions from one observable and returns those of another observable. Confused? Look:
        query.flatMap(new Func1<List<String>, Observable<?>>() {
            @Override
            public Observable<?> call(List<String> strings) {
                return Observable.from(strings);
            }
        }).subscribe(System.out::println);
        // So, .flatmap() took an observable that emits lists, and returned an observable that emits the items of a
        // list by using Observable.from(), which we then subscribe to. Powerful!
    }

    /**
     * Same as test3, but in compact form.
     */
    private void test4() {
        System.out.println("Playground2 4.");
        // Of course we can use lambdas!
        query.flatMap(Observable::from)
             .subscribe(System.out::println);
        // It's awesome, right!?
    }
}
