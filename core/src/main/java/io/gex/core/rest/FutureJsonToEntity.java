package io.gex.core.rest;


import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class FutureJsonToEntity<T> implements GxFuture<T> {
    private GxFuture<JsonObject> responseToJson;

    public FutureJsonToEntity(GxFuture<JsonObject> responseToJson) {
        this.responseToJson = responseToJson;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return responseToJson.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return responseToJson.isCancelled();
    }

    @Override
    public boolean isDone() {
        return responseToJson.isDone();
    }

    @Override
    public T get() throws InterruptedException, GexException {
        return afterGetJson(responseToJson.get());
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, GexException {
        return afterGetJson(responseToJson.get(timeout, unit));
    }

    public abstract T afterGetJson(JsonObject response)throws GexException;
}
