package bkromhout.rxjava;

import com.squareup.okhttp.*;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Transforms OkHttp Requests to Responses. Simply returns null if the request fails.
 */
public class RxOkHttpCall implements Observable.Transformer<Request, Response> {
    private final OkHttpClient client;

    public RxOkHttpCall(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<Response> call(Observable<Request> requests) {
        return requests.flatMap(request -> Observable.create(new OnSubscribeEnqueue(client, request)));
    }

    /**
     * Allows for easy creation of Observables which return OkHttp Responses.
     */
    public static final class OnSubscribeEnqueue implements Observable.OnSubscribe<Response> {
        private final OkHttpClient client;
        private final Request request;
        private final Executor cancellationExecutor;

        /**
         * Create a new OnSubscribeEnqueue which uses the Executor from the OkHttpClient dispatcher.
         * @param request Request to enqueue.
         */
        public OnSubscribeEnqueue(OkHttpClient client, Request request) {
            this(client, request, client.getDispatcher().getExecutorService());
        }

        /**
         * Create a new OnSubscribeEnqueue with a specific Executor.
         * @param request              Request to enqueue.
         * @param cancellationExecutor Executor of the OkHttpClient.
         */
        public OnSubscribeEnqueue(OkHttpClient client, Request request, Executor cancellationExecutor) {
            this.client = client;
            this.request = request;
            this.cancellationExecutor = cancellationExecutor;
        }

        @Override
        public void call(final Subscriber<? super Response> sub) {
            // Return right away if null.
            if (request == null) {
                sub.onNext(null);
                sub.onCompleted();
                return;
            }
            // Create OkHttp Call.
            final Call call = client.newCall(request);
            // Make sure that the request is cancelled when unsubscribing.
            sub.add(Subscriptions.create(() -> cancellationExecutor.execute(call::cancel)));
            // Enqueue the call.
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    sub.onNext(null);
                    sub.onCompleted();
                }

                @Override
                public void onResponse(Response resp) throws IOException {
                    if (sub.isUnsubscribed()) return;
                    // Make sure the response is actually valid.
                    if (!resp.isSuccessful()) sub.onNext(null);
                    // If we were successful, notify the subscriber and then indicate we're complete.
                    sub.onNext(resp);
                    sub.onCompleted();
                }
            });
        }
    }
}