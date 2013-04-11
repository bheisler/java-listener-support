package com.github.javalistenersupport;

/**
 * Interface for classes that hold collections of listeners.
 */
public interface CollectionHolder<T> extends Iterable<T>
{
    /**
     * Add a new listener to the collection
     */
    void registerListener( T listener );

    /** Remove a listener from the collection */
    void unregisterListener( T listener );

}
