package io.gex.core.rest;


import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;

import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class FutureResponseToJson implements GxFuture<JsonObject> {

    private final static LogWrapper logger = LogWrapper.create(FutureResponseToJson.class);

    private Future<Response> responseFuture;
    private String url;
    private LogType type;
    private String requestType;
    private boolean authenticated;

    FutureResponseToJson(Future<Response> responseFuture, String url, LogType type, String requestType,
                         boolean authenticated) {
        this.responseFuture = responseFuture;
        this.url = url;
        this.type = type;
        this.requestType = requestType;
        this.authenticated = authenticated;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return responseFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return responseFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return responseFuture.isDone();
    }

    //todo implement reconnect logic
    @Override
    public JsonObject get() throws InterruptedException, GexException {
        try {
            return Rest.processResponse(responseFuture.get(), url, type, requestType, authenticated).getAsJsonObject();
        } catch (RuntimeException | ExecutionException e) {
            throw logger.logAndReturnException(CoreMessages.SERVER_CONNECTION_ERROR, e, LogType.SERVER_RESPONSE_ERROR);
        }
    }

    //todo implement reconnect logic
    @Override
    public JsonObject get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, GexException {
        try {
            return Rest.processResponse(responseFuture.get(timeout, unit), url, type, requestType, authenticated)
                    .getAsJsonObject();
        } catch (RuntimeException | ExecutionException e) {
            throw logger.logAndReturnException(CoreMessages.SERVER_CONNECTION_ERROR, e, LogType.SERVER_RESPONSE_ERROR);
        }
    }

}

