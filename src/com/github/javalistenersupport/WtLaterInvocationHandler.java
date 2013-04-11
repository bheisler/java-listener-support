package com.github.javalistenersupport;

import java.lang.reflect.Method;

/**
 * This is an InvocationHandler that ensures that listener methods are always called
 * asynchronously on a worker thread.
 */
class WtLaterInvocationHandler<T> extends DefaultInvocationHandler<T>
{
    public WtLaterInvocationHandler( Iterable<T> listeners )
    {
        super( listeners );
    }

    @Override
    protected void doIteration( final Method method, final Object[] args ) throws Throwable
    {
        Runnable wtRunnable = new InvocationHandlerRunnable( method, args );
        new Thread( wtRunnable ).start( );

    }
}
