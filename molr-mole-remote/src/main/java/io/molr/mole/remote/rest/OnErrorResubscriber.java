package io.molr.mole.remote.rest;

import java.util.concurrent.Executors;
import java.util.function.Supplier;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a new flux and connects a provided flux to it. If the provided flux fails the local flux is provided with an
 * backup value until a new subscription has been established.

 * @param <T> the type of streamed items
 */
public class OnErrorResubscriber<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnErrorResubscriber.class);
    private ReplayProcessor<T> flux;
    private T onErrorValue;
    private Supplier<Flux<T>> fluxProvider;

    public OnErrorResubscriber(T onErrorValue, Supplier<Flux<T>> fluxProvider) {
        this.fluxProvider = fluxProvider;
        this.onErrorValue = onErrorValue;
        this.flux = ReplayProcessor.cacheLast();
        subscribeToProvidedFlux();
    }

    private void subscribeToProvidedFlux() {
        fluxProvider.get().subscribe(agencyState -> {
            flux.onNext(agencyState);
        }, error -> {
            System.out.println("error " + error);
            flux.onNext(onErrorValue);
            Executors.newSingleThreadExecutor().submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                        subscribeToProvidedFlux();
                    } catch (Exception e) {
                        LOGGER.info("Exception while trying to subscribe to provided flux");
                    }
                }
            });
        }, () -> {
            LOGGER.info("Connected flux is complete");
            flux.onComplete();
        });
    }

    public Flux<T> flux() {
        return flux;
    }

}
