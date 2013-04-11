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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import lombok.RequiredArgsConstructor;

/**
 * Invocation handler that invokes listeners on several worker threads
 * using the Fork/Join framework.
 */
class ParallelInvocationHandler<T> extends DefaultInvocationHandler<T> {

    private final ForkJoinPool pool = new ForkJoinPool( );

    public ParallelInvocationHandler( Iterable<T> listeners ) {
        super( listeners );
    }

    @Override
    protected void doIteration( Method method, Object[] args ) throws Throwable {
        pool.submit( new InvocationAction( getListeners( ), method, args ) );
    }

    @RequiredArgsConstructor
    private class InvocationAction extends RecursiveAction {

        private final Iterable<T> listeners;
        private final Method method;
        private final Object[] args;

        @Override
        protected void compute( ) {
            List<ForkJoinTask<?>> tasks = new ArrayList<>( );
            for ( T listener : listeners ) {
                tasks.add( new SingleInvocationTask( listener, method, args ) );
            }
            invokeAll( tasks );

        }
    }

    @RequiredArgsConstructor
    private class SingleInvocationTask extends RecursiveAction {

        private final T listener;
        private final Method method;
        private final Object[] args;

        @Override
        public void compute( ) {
            try {
                invokeMethod( listener, method, args );
            }
            catch ( Throwable t ) {
                t.printStackTrace( );
                completeExceptionally( t );
            }
        }

    }
}
