package bkromhout.rxjava;

import com.github.davidmoten.rx.Checked;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.util.async.Async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Schedulers and threading
 */
public class Playground4 {

    ArrayList<String> urls = new ArrayList<>(Arrays.asList("https://www.google.com", "https://www.facebook.com",
            "https://www.yahoo.com", "https://www.fanfiction.net", "http://siye.co.uk", "fakesite"));

    public void run() {
        test1();
        test2();
        test3();
        test4();
        test5();
    }

    /**
     * Basic schedulers.
     */
    private void test1() {
        System.out.println("Playground4 1.");
        // We're doing it on a background thread, which is great, but we're still doing everything one at a time, so
        // it's not really async.
        Observable.from(urls)
                  .map(Checked.f1(this::getHtml))
                  .map(this::getTitleFromDoc)
                  .subscribeOn(Schedulers.io())
                  .observeOn(Schedulers.immediate())
                  .toBlocking() // Wait plz.
                  .subscribe(
                          System.out::println,
                          (error) -> System.out.println("Ouch!"),
                          () -> System.out.println("Done!")
                  );
    }

    /**
     * Same as test1, but async
     */
    private void test2() {
        System.out.println("\nPlayground4 2.");
        Observable.from(urls)
                  .subscribeOn(Schedulers.io())
                  .flatMap(Async.toAsync(this::getTitleFromDlDoc, Schedulers.io()))
                  .observeOn(Schedulers.immediate())
                  .toBlocking() // Wait plz.
                  .subscribe(
                          System.out::println,
                          (error) -> System.out.println("Ouch!"),
                          () -> System.out.println("Done!")
                  );
    }

    /**
     * Same as test2, but different impl.
     */
    private void test3() {
        System.out.println("\nPlayground4 3.");
        Observable.from(urls)
                  .subscribeOn(Schedulers.newThread())
                  .flatMap(Async.toAsync(this::safeGetHtml, Schedulers.io()))
                  .flatMap(Async.toAsync(this::safeGetTitleFromDoc, Schedulers.computation()))
                  .observeOn(Schedulers.immediate())
                  .toBlocking()
                  .subscribe(
                          System.out::println,
                          (error) -> System.out.println("Ouch!"),
                          () -> System.out.println("Done!")
                  );
    }

    /**
     * Same as test3, but sorts output.
     */
    private void test4() {
        System.out.println("\nPlayground4 4.");
        Observable<List<String>> sorted = Observable.defer(() -> Observable
                .from(urls)
                .subscribeOn(Schedulers.newThread())
                .flatMap(Async.toAsync(this::safeGetHtml, Schedulers.io()))
                .flatMap(Async.toAsync(this::safeGetTitleFromDoc, Schedulers.computation()))
                .observeOn(Schedulers.immediate())
                .toSortedList()
        );
        sorted.flatMap(Observable::from)
              .toBlocking()
              .subscribe(
                      System.out::println,
                      (error) -> System.out.println("Ouch!"),
                      () -> System.out.println("Done!")
              );
    }

    /**
     * Same as test4, but uses futures.
     */
    private void test5() {
        System.out.println("\nPlayground4 5.");
        Future<List<String>> sortedList = Observable
                .from(urls)
                .subscribeOn(Schedulers.newThread())
                .flatMap(Async.toAsync(this::safeGetHtml, Schedulers.io()))
                .flatMap(Async.toAsync(this::safeGetTitleFromDoc, Schedulers.computation()))
                .observeOn(Schedulers.immediate())
                .toSortedList()
                .toBlocking()
                .toFuture();

        try {
            Observable.from(sortedList.get())
                      .subscribe(
                              System.out::println,
                              (error) -> System.out.println("Ouch!"),
                              () -> System.out.println("Done!")
                      );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document getHtml(String url) throws IOException {
        Connection connection = Jsoup.connect(url);
        return connection.get();
    }

    private Document safeGetHtml(String url) {
        try {
            return getHtml(url);
        } catch (Throwable t) {
            return null;
        }
    }

    private String getTitleFromDoc(Document doc) {
        return doc.select("title").first().text();
    }

    private String safeGetTitleFromDoc(Document doc) {
        if (doc == null) return "Null doc.";
        return getTitleFromDoc(doc);
    }

    private String getTitleFromDlDoc(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            return connection.get().select("title").first().text();
        } catch (Throwable t) {
            return "Invalid Url: " + url;
        }
    }
}
