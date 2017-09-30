package io.gex.core;

import io.gex.core.exception.GexException;

@FunctionalInterface
public interface GexExecutorOutput {
    String execute() throws GexException;
}
