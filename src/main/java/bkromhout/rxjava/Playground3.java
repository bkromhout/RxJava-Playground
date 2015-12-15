package bkromhout.rxjava;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;

/**
 * Now with error handling!
 */
public class Playground3 {

    /**
     * Yay!
     */
    public void run() {
        test1();
        test2();
    }

    /**
     * This one errors out.
     */
    private void test1() {
        System.out.println("Playground3 1.");
        Observable.just("Hello, world! 1")
                  .map(s -> {
                      try {
                          return couldThrow(s);
                      } catch (Throwable t) {
                          throw Exceptions.propagate(t);
                      }
                  })
                  .subscribe(new Subscriber<String>() {
                      @Override
                      public void onCompleted() {
                          System.out.println("Complete!");
                      }

                      @Override
                      public void onError(Throwable e) {
                          System.out.println("Ouch!");
                      }

                      @Override
                      public void onNext(String s) {
                          System.out.println(s);
                      }
                  });
    }

    /**
     * This one completes.
     */
    private void test2() {
        System.out.println("Playground3 2.");
        Observable.just("Hello, world! 2")
                  .map(s -> {
                      try {
                          return couldThrow(s);
                      } catch (Throwable t) {
                          throw Exceptions.propagate(t);
                      }
                  })
                  .subscribe(new Subscriber<String>() {
                      @Override
                      public void onCompleted() {
                          System.out.println("Complete!");
                      }

                      @Override
                      public void onError(Throwable e) {
                          System.out.println("Ouch!");
                      }

                      @Override
                      public void onNext(String s) {
                          System.out.println(s);
                      }
                  });
    }


    private String couldThrow(String s) throws Exception {
        if (s.contains("1")) throw new Exception();
        return s + " BLUE!";
    }
}
