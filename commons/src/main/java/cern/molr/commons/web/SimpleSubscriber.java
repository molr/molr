package cern.molr.commons.web;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A simple subscriber which requests one element after subscription and one element after each received element
 * @param <T> the element type
 * @author yassine-kr
 */
public abstract class SimpleSubscriber<T> implements Subscriber<T> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription=subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(T t) {
        onConsume(t);
        subscription.request(1);
    }

    public abstract void onConsume(T t);
}
