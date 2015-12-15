package bkromhout.rxjava;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * More schedulers and threading, with OkHttp.
 */
public class Playground5 {

    OkHttpClient client;

    ArrayList<String> urls = new ArrayList<>(
            Arrays.asList("https://www.google.com", "https://www.facebook.com", "http://siye.co.uk", "fakesite",
                    "https://www.yahoo.com", "https://www.fanfiction.net", "http://fanfiction.mugglenet.com"));

    public void run() {
        client = new OkHttpClient();
        client.getDispatcher().setMaxRequests(3);
        client.setReadTimeout(0, TimeUnit.MILLISECONDS);
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC);
        client.interceptors().add(logger);

        test1();

        client.getDispatcher().getExecutorService().shutdownNow();
    }

    private void test1() {
        System.out.println("\nPlayground5 1.");
        //Future<List<String>> sortedList = Observable
        List<String> sortedList = Observable
                .from(urls)
                .subscribeOn(Schedulers.newThread())
                .map(this::makeRequest) // Create OkHttp Requests from URLs.
                .compose(new RxOkHttpCall(client)) // Get Responses by executing Requests.
                //.observeOn(Schedulers.computation())
                .map(this::parseDocFromRespBody)
                .map(this::safeGetTitleFromDoc)
                .toList()
                .observeOn(Schedulers.immediate())
                .toBlocking()
                .single();
        //.toFuture();

        try {
            Observable.from(sortedList/*.get()*/)
                      .subscribe(
                              System.out::println,
                              (error) -> System.out.println("Ouch!"),
                              () -> System.out.println("Done!"))
                      .unsubscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Request makeRequest(String url) {
        try {
            return new Request.Builder().url(url).build();
        } catch (Exception e) {
            return null;
        }
    }

    private Document parseDocFromRespBody(Response resp) {
        if (resp == null) return null;
        try {
            return Jsoup.parse(resp.body().byteStream(), null, resp.request().url().toString());
        } catch (IOException e) {
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
}
