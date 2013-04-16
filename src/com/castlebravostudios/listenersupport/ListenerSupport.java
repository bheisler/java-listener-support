/*
 * Copyright (C) 2012 Brook Heisler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.castlebravostudios.listenersupport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.Getter;

/**
 * A class to make implementation of the Observer pattern easier.
 * ListenerSupport handles the thread safety issues and uses dynamic proxying to
 * make it easy to fire events. This class implements Iterable<T> in order to allow
 * client code to iterate over the contained listeners if necessary.<p>
 *
 * Clients should ensure that listeners will not throw exceptions (which is generally
 * good practice anyway). An exception thrown by a listener will, unless otherwise
 * noted, terminate iteration of the listeners. The specific exception handling
 * behavior of each fire method will be noted in the documentation for that method.<p>
 *
 * Example Usage:<br><code>
 * ListenerSupport&lt;MyListener&gt; support = new ListenerSupport&lt;&gt;(MyListener.class);<br>
 * support.fire().myListenerMethod();<br>
 * support.fireOnEdtLater().myListenerMethod2( MyEventObject obj );<br>
 * support.filter(new MyFilter1()).filter(new MyFilter2()).fire().myListenerMethod();<br>
 * </code>
 * <br>
 * This class is thread-safe if and only if the collection handler is thread safe.
 * The default collection handlers {@link CopyOnWriteSetHolder} and
 * {@link WeakCollectionHolder} are both thread-safe.
 */
public final class ListenerSupport<T> implements Iterable<T> {
    private final Map<Class<?>, T> proxyCache = new HashMap<>();

    @Getter private final Class<T> listenerClass;

    private final Class<T> proxyClass;

    private final CollectionHolder<T> collection;

    /**
     * Public constructor provided in case clients wish to use their own
     * CollectionHolder. This is not recommended, as the standard holders
     * should suffice for the majority of users.
     */
    @SuppressWarnings( "unchecked" )
    public ListenerSupport( Class<T> listenerClass, CollectionHolder<T> holder ) {
        assert( listenerClass.isInterface( ) ) : "Must use a listener interface.";
        this.listenerClass = listenerClass;
        this.proxyClass = (Class<T>)Proxy.getProxyClass( listenerClass.getClassLoader( ), listenerClass );
        this.collection = holder;
    }

    /**
     * Private constructor to support filtering.
     */
    private ListenerSupport( Class<T> listenerClass, Class<T> proxyClass, CollectionHolder<T> holder ) {
        this.listenerClass = listenerClass;
        this.proxyClass = proxyClass;
        this.collection = holder;
    }

    public void registerListener( T listener ) {
        collection.registerListener( listener );
    }

    public void unregisterListener( T listener ) {
        collection.unregisterListener( listener );
    }

    @Override
    public Iterator<T> iterator( ) {
        return collection.iterator( );
    }

    public int size( ) {
        return collection.size( );
    }

    /**
     * Returns a filtered view of the current ListenerSupport which can be used
     * to fire events to only a subset of listeners. Registrations made in this
     * ListenerSupport will be reflected in the returned view, but listeners
     * cannot be registered or unregistered with the view.
     */
    public ListenerSupport<T> filter( ListenerFilter<T> filter ) {
        return new ListenerSupport<T>( listenerClass, proxyClass,
                new FilteredCollectionHolder<T>( collection, filter ) );
    }

    /**
     * Returns a proxy listener that will forward all method calls to all listeners.
     * This method fires events on the calling thread. Any exceptions thrown by
     * listeners will be propagated from this method.<br>
     *
     * Note that this method, like all of the fire methods, uses reflection-based
     * proxying and is likely unsuitable for high-performance or high-security
     * environments.
     */
    @SuppressWarnings( "unchecked" )
    public T fire() {
        return getProxy( (Class<? extends DefaultInvocationHandler<T>>)
                DefaultInvocationHandler.class );
    }

    /**
     * Returns a proxy listener that will forward all method calls to all listeners.
     * This method fires events asynchronously on the event dispatch thread. <b>Any
     * exceptions thrown by listeners will be printed to System.err and then ignored!</b><br>
     *
     * Note that this method, like all of the fire methods, uses reflection-based
     * proxying and is likely unsuitable for high-performance or high-security
     * environments.
     */
    @SuppressWarnings( "unchecked" )
    public T fireOnEdtLater() {
        return getProxy( (Class<? extends DefaultInvocationHandler<T>>)
                EdtLaterInvocationHandler.class );
    }

    /**
     * Returns a proxy listener that will forward all method calls to all listeners.
     * This method fires events synchronously on the event dispatch thread. Any
     * exceptions thrown by listeners will be rethrown on the calling thread.<br>
     *
     * Note that this method, like all of the fire methods, uses reflection-based
     * proxying and is likely unsuitable for high-performance or high-security
     * environments.
     */
    @SuppressWarnings( "unchecked" )
    public T fireOnEdtAndWait() {
        return getProxy( (Class<? extends DefaultInvocationHandler<T>>)
                EdtAndWaitInvocationHandler.class );
    }

    /**
     * Returns a proxy listener that will forward all method calls to all listeners.
     * This method fires events asynchronously on a worker thread. <b>Any
     * exceptions thrown by listeners will be printed to System.err and then ignored!</b><br>
     *
     * Note that this method, like all of the fire methods, uses reflection-based
     * proxying and is likely unsuitable for high-performance or high-security
     * environments.
     */
    @SuppressWarnings( "unchecked" )
    public T fireOnWtLater() {
        return getProxy( (Class<? extends DefaultInvocationHandler<T>>)
                WtLaterInvocationHandler.class );
    }

    /**
     * Returns a proxy listener that will forward all method calls to all listeners.
     * This method fires events asynchronously on several worker threads using the
     * Fork/Join framework. <b>Any exceptions thrown by listeners will be printed
     * to System.err and then ignored, but will not prevent other listeners from
     * being executed.</b> This proxy method is best suited for large numbers of
     * listeners or listeners which are expected to take a long time.<br>
     *
     * Note that this method, like all of the fire methods, uses reflection-based
     * proxying and is likely unsuitable for high-performance or high-security
     * environments.
     */
    @SuppressWarnings( "unchecked" )
    public T fireInParallel() {
        return getProxy( (Class<? extends DefaultInvocationHandler<T>>)
                ParallelInvocationHandler.class );
    }

    /**
     * Returns a proxy listener that will forward all method calls to all listeners
     * using the given proxy. The exception-handling and thread-safety of the
     * given handler are entirely dependent on the definition of the handler.
     *
     * Note that this method, like all of the fire methods, uses reflection-based
     * proxying and is likely unsuitable for high-performance or high-security
     * environments.
     */
    protected T fireWithHandler( Class<? extends DefaultInvocationHandler<T>> cls ) {
        return getProxy( cls );
    }

    private T getProxy( Class<? extends DefaultInvocationHandler<T>> cls ) {
        if ( proxyCache.containsKey( cls ) ) {
            return proxyCache.get( cls );
        }

        try {
            Constructor<? extends DefaultInvocationHandler<T>> constructor =
                    cls.getConstructor( Iterable.class );
            DefaultInvocationHandler<T> instance = constructor.newInstance( collection );
            T proxy = getProxy( instance );
            proxyCache.put( cls, proxy );
            return proxy;
        }
        catch ( NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e ) {
            throw new ProxyException(e);
        }
    }

    private T getProxy( DefaultInvocationHandler<T> handler ) {
        try {
            return proxyClass.getConstructor( InvocationHandler.class ).newInstance( handler );
        }
        catch ( InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e ) {
            throw new ProxyException( e );
        }
    }

    /**
     * Returns a ListenerSupport backed by a CopyOnWriteArraySet of listeners.
     * Listeners are strongly-referenced and must be unregistered manually.
     * This should be sufficient for most uses. ListenerSupports returned from
     * this method are thread-safe.
     */
    public static <T> ListenerSupport<T> create( Class<T> listenerClass ) {
        return new ListenerSupport<T>( listenerClass, new CopyOnWriteSetHolder<T>( ) );
    }

    /**
     * Returns a ListenerSupport backed by a CopyOnWriteArraySet of WeakReferences
     * to listeners. Listeners will be removed automatically as they become weakly
     * reachable. ListenerSupports returned from this method are thread-safe.
     */
    public static <T> ListenerSupport<T> createWeak( Class<T> listenerClass ) {
        return new ListenerSupport<T>( listenerClass, new WeakCollectionHolder<T>( ) );
    }
}
