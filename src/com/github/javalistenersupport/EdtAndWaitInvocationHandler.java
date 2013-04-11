package com.github.javalistenersupport;

import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

/**
 * This is an invocation handler that fires events on the EDT synchronously and
 * rethrows all caught exceptions on the calling thread.
 */
class EdtAndWaitInvocationHandler<T> extends DefaultInvocationHandler<T>
{

    public EdtAndWaitInvocationHandler( Iterable<T> listeners )
    {
        super( listeners );
    }

    @Override
    protected void doIteration( Method method, Object[] args ) throws Throwable
    {
        InvocationHandlerRunnable runnable = new InvocationHandlerRunnable( method, args );
        SwingUtilities.invokeAndWait( runnable );
        if ( runnable.getException( ) != null ) {
            throw new Throwable( runnable.getException( ) );
        }
    }
}
