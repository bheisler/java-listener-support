package com.github.javalistenersupport;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.RequiredArgsConstructor;
import com.google.common.collect.ForwardingIterator;

/**
 * Default CollectionHolder.
 */
class CopyOnWriteSetHolder<T> implements CollectionHolder<T>
{
    private final CopyOnWriteArraySet<T> listeners = new CopyOnWriteArraySet<>( );

    @Override
    public void registerListener( T listener )
    {
        listeners.add( listener );
    }

    @Override
    public void unregisterListener( T listener )
    {
        listeners.remove( listener );
    }

    @Override
    public Iterator<T> iterator( )
    {
        return new UnmodifiableIterator( listeners.iterator( ) );
    }

    @RequiredArgsConstructor
    private final class UnmodifiableIterator extends ForwardingIterator<T>
    {
        private final Iterator<T> delegate;

        @Override
        protected Iterator<T> delegate( )
        {
            return delegate;
        }

        @Override
        public void remove( )
        {
            throw new UnsupportedOperationException( );
        }

    }
}
