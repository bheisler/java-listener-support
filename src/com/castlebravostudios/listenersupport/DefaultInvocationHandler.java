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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the iteration and method invocation handling.
 * If you need to change the way events are dispatched to listeners,
 * override this.
 *
 * All subclasses MUST provide a one-argument constructor that takes an Iterable<T>.
 */
@RequiredArgsConstructor
public class DefaultInvocationHandler<T> implements InvocationHandler {
    @Getter private final Iterable<T> listeners;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable {
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
        catch ( InvocationTargetException e ) {
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
        public void run( ) {
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
