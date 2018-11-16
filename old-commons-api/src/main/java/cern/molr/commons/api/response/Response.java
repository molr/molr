package cern.molr.commons.api.response;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a generic response sent by the server
 *
 * @param <R> the type of the returned object
 */
public class Response<R> {

    private final R result;
    private final Throwable throwable;
    private final boolean success;

    public Response(R result, Throwable throwable, boolean success) {
        this.result = result;
        this.throwable = throwable;
        this.success = success;
    }

    public Response(R result) {
        this.result = result;
        this.throwable = null;
        this.success = true;
    }

    public Response(Throwable throwable) {
        this.result = null;
        this.throwable = throwable;
        this.success = false;
    }

    public R getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isSuccess() {
        return success;
    }

    public final <T> T match(Function<Throwable, T> failureMapper, Function<R, T> successMapper) {
        if (success) {
            return successMapper.apply(result);
        } else {
            return failureMapper.apply(throwable);
        }
    }


    public final void execute(Consumer<Throwable> failureConsumer, Consumer<R> successConsumer) {
        if (success) {
            successConsumer.accept(result);
        } else {
            failureConsumer.accept(throwable);
        }
    }
}
