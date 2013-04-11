package com.github.javalistenersupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Iterator;
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
 */
public final class ListenerSupport<T> implements Iterable<T>
{
    @Getter private final Class<T> listenerClass;

    private final Class<T> proxyClass;

    private final CollectionHolder<T> collection;

    /**
     * Public constructor provided in case clients wish to use their own
     * CollectionHolder. This is not recommended, as the standard holders
     * should suffice for the majority of users.
     */
    @SuppressWarnings( "unchecked" )
    public ListenerSupport( Class<T> listenerClass, CollectionHolder<T> holder )
    {
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
    public Iterator<T> iterator( )
    {
        return collection.iterator( );
    }

    /**
     * Returns a filtered view of the current ListenerSupport which can be used
     * to fire events to only a subset of listeners.
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
    public T fire() {
        return getProxy( new DefaultInvocationHandler<>( collection ) );
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
    public T fireOnEdtLater() {
        return getProxy( new EdtLaterInvocationHandler<T>( collection ) );
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
    public T fireOnEdtAndWait() {
        return getProxy( new EdtAndWaitInvocationHandler<T>( collection ) );
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
    public T fireOnWtLater() {
        return getProxy( new WtLaterInvocationHandler<T>( collection ) );
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
    public T fireInParallel() {
        return getProxy( new ParallelInvocationHandler<T>( collection ) );
    }

    private T getProxy( DefaultInvocationHandler<T> handler ) {
        try
        {
            return proxyClass.getConstructor( InvocationHandler.class ).newInstance( handler );
        }
        catch ( InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e )
        {
            throw new ProxyException( e );
        }
    }

    /**
     * Returns a ListenerSupport backed by a CopyOnWriteArraySet of listeners.
     * This should be sufficient for most uses.
     */
    public static <T> ListenerSupport<T> newListenerSupport( Class<T> listenerClass ) {
        return new ListenerSupport<T>( listenerClass, new CopyOnWriteSetHolder<T>( ) );
    }

    /**
     * Returns a ListenerSupport backed by a CopyOnWriteArraySet of WeakReferences
     * to listeners. Listeners will be removed automatically as they become Weakly
     * reachable.
     */
    public static <T> ListenerSupport<T> newWeakListenerSupport( Class<T> listenerClass ) {
        return new ListenerSupport<T>( listenerClass, new WeakCollectionHolder<T>( ) );
    }
}
