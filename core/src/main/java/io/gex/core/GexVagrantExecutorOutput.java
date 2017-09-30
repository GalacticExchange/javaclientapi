package io.gex.core;

import io.gex.core.exception.GexException;

@FunctionalInterface
public interface GexVagrantExecutorOutput {
    String execute() throws GexException;
}
