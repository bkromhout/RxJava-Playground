package bkromhout.rxjava;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * A playground with very basic examples.
 */
public class Playground1 {

    /**
     * Play.
     */
    public void run() {
        // The many ways to print "Hello, world!", from verbose to concise.
        test1();
        test2();
        test3();
        test4();
        // Transformations.
        test5();
        test6();
        test7();
    }

    /**
     * Very basic example, if somewhat verbose.
     */
    private void test1() {
        // Create an observable.
        Observable<String> myObservable = Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("Hello, world! 1");
                        subscriber.onCompleted();
                    }
                }
        );
        // Create a subscriber.
        Subscriber<String> mySubscriber = new Subscriber<String>() {
            @Override
            public void onNext(String s) {
                // Print any strings we get.
                System.out.println(s);
            }

            @Override
            public void onCompleted() {
                // Do nothing.
            }

            @Override
            public void onError(Throwable e) {
                // Do nothing.
            }
        };
        // Link them up.
        myObservable.subscribe(mySubscriber);
    }

    /**
     * The same functionality as test1, but more compact.
     */
    private void test2() {
        // Create an observable using .just(), which only emits the given items and then completes.
        Observable<String> myObservable = Observable.just("Hello, world! 2");
        // We only care about the onNext action, so instead of creating a whole subscriber, we'll just create that
        // action...
        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        };
        // ...and then subscribe using it.
        myObservable.subscribe(onNextAction);
        // It's that simple! (note, it actually can be simpler, see test3)
    }

    /**
     * Same as test1 and test2, but even more compact.
     */
    private void test3() {
        // All in one go, without lambdas.
        Observable.just("Hello, world! 3")
                  .subscribe(new Action1<String>() {
                      @Override
                      public void call(String s) {
                          System.out.println(s);
                      }
                  });
    }

    /**
     * Same as test1, test2, and test3, but even more compact.
     */
    private void test4() {
        // All in one go, with lambdas. Incredible, right?!
        Observable.just("Hello, world! 4")
                  .subscribe(System.out::println);
    }

    /**
     * Prints hello world, but transforms it first by using the subscriber (not good).
     */
    private void test5() {
        // We want to append, but don't have access to the Observer (in theory, obviously), so...
        Observable.just("Hello, world!")
                  .subscribe(s -> System.out.println(s + " 5"));
        // However, subscribers are supposed to be as simple as possible, so this isn't really the best choice.
    }

    /**
     * Operators!
     */
    private void test6() {
        // Since we want the subscriber to be lightweight, we can use the .map operator to append the string for us.
        Observable.just("Hello, world!")
                  .map(s -> s + " 6")
                  .subscribe(System.out::println);
    }

    /**
     * More .map() fun. Our subscriber doesn't have to receive the same type that the observable produces.
     */
    private void test7() {
        // Say we want to output the hashcode of the string instead of the string
        Observable.just("Hello, world!")
                  .map(s -> s + " 7")
                  .map(String::hashCode)
                  .subscribe(i -> System.out.println(Integer.toString(i)));
        // Of course, technically we'd want to add another map call before subscribe to do the Integer.toString()
        // outside of subscribe, but for the sake of this example, we won't.
    }


}
