package com.github.javalistenersupport;

/**
 * Interface that must be implemented by any class which wishes
 * to act as a filter for Listeners.
 */
public interface ListenerFilter<T>
{
    /**
     * Return true if the listener meets the criteria for passing
     * this filter.
     */
    boolean passesFilter( T listener );
}
