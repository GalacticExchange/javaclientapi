package io.gex.core;

import io.gex.core.exception.GexException;

@FunctionalInterface
public interface GexExecutor {
    void execute() throws GexException;
}
