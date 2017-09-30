package io.gex.core.rest;


import io.gex.core.exception.GexException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
*
* Analog to java.util.concurrent.Future, but throws GexAuthException, GexException and doesn't throw ExecutionException
 */
public interface GxFuture<T> {

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    T get() throws InterruptedException, GexException;

    T get(long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException, GexException;
}
