package org.omnifix.duck;

/**
 * Implemented by {@code ServerFunctionManager} when {@code feature.mcfunction_profiling} is active.
 */
public interface IProfilingServerFunctionManager {
    String omnifix$getProfilingResults();
}
