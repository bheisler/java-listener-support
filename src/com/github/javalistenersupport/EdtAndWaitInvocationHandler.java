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
package com.github.javalistenersupport;

import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

/**
 * This is an invocation handler that fires events on the EDT synchronously and
 * rethrows all caught exceptions on the calling thread.
 */
class EdtAndWaitInvocationHandler<T> extends DefaultInvocationHandler<T> {

    public EdtAndWaitInvocationHandler( Iterable<T> listeners ) {
        super( listeners );
    }

    @Override
    protected void doIteration( Method method, Object[] args ) throws Throwable {
        InvocationHandlerRunnable runnable = new InvocationHandlerRunnable( method, args );
        SwingUtilities.invokeAndWait( runnable );
        if ( runnable.getException( ) != null ) {
            throw new Throwable( runnable.getException( ) );
        }
    }
}
