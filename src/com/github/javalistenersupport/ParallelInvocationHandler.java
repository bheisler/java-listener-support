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
class ParallelInvocationHandler<T> extends DefaultInvocationHandler<T>
{

    private final ForkJoinPool pool = new ForkJoinPool( );

    public ParallelInvocationHandler( Iterable<T> listeners )
    {
        super( listeners );
    }

    @Override
    protected void doIteration( Method method, Object[] args ) throws Throwable
    {
        pool.submit( new InvocationAction( getListeners( ), method, args ) );
    }

    @RequiredArgsConstructor
    private class InvocationAction extends RecursiveAction {

        private final Iterable<T> listeners;
        private final Method method;
        private final Object[] args;

        @Override
        protected void compute( )
        {
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
        public void compute( )
        {
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
