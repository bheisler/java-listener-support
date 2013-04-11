package com.github.javalistenersupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the iteration and method invocation handling.
 * If you need to change the way events are dispatched to listeners,
 * override this.
 */
@RequiredArgsConstructor
public class DefaultInvocationHandler<T> implements InvocationHandler
{
    @Getter private final Iterable<T> listeners;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
            {
        doIteration( method, args );
        return null;
            }

    /**
     * This method performs the iteration over the listener set. Override this if you
     * need to iterate over the set in a different way. Call invokeMethod on each
     * listener, if possible.
     */
    protected void doIteration( Method method, Object[] args ) throws Throwable {
        for ( T listener : listeners ) {
            invokeMethod( listener, method, args );
        }
    }

    /**
     * This method is responsible for calling the listener method. Most implementations will
     * need to override this method. If isWeak is true, this method will get the referent from
     * the weak listener and use that, if it is non-null.
     */
    protected void invokeMethod( T listener, Method method, Object[] args ) throws Throwable {
        try {
            method.invoke( listener, args );
        }
        catch ( InvocationTargetException e )
        {
            throw e.getTargetException( );
        }
    }

    /**
     * Runnable class that simply calls doIteration. Use this to put the iteration
     * on a different thread. If the iteration throws an exception, it will be
     * printed to {@link System#err}, captured and stored in exception.
     */
    @RequiredArgsConstructor
    protected class InvocationHandlerRunnable implements Runnable {

        @Getter private Throwable exception;
        private final Method method;
        private final Object[] args;

        @Override
        public void run( )
        {
            try {
                doIteration( method, args );
            }
            catch ( Throwable t ) {
                //This should not happen.
                exception = t;
                t.printStackTrace( );
            }
        }

    }

}
