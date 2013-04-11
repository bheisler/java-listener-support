package com.github.javalistenersupport;

import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

/**
 * This is an InvocationHandler that ensures that listener methods are always called
 * asynchronously on the event dispatch thread.
 */
class EdtLaterInvocationHandler<T> extends DefaultInvocationHandler<T>
{

    public EdtLaterInvocationHandler( Iterable<T> listeners )
    {
        super( listeners );
    }

    @Override
    protected void doIteration( final Method method, final Object[] args ) throws Throwable
    {
        Runnable edtRunnable = new InvocationHandlerRunnable( method, args );
        SwingUtilities.invokeLater( edtRunnable );
    }
}
