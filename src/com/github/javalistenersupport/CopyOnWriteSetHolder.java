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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.RequiredArgsConstructor;
import com.google.common.collect.ForwardingIterator;

/**
 * Default CollectionHolder. Holds listeners in a CopyOnWriteArraySet.
 * <br>
 * This class is thread-safe.
 */
class CopyOnWriteSetHolder<T> implements CollectionHolder<T> {
    private final CopyOnWriteArraySet<T> listeners = new CopyOnWriteArraySet<>( );

    @Override
    public void registerListener( T listener ) {
        listeners.add( listener );
    }

    @Override
    public void unregisterListener( T listener ) {
        listeners.remove( listener );
    }

    @Override
    public Iterator<T> iterator( ) {
        return new UnmodifiableIterator( listeners.iterator( ) );
    }

    @Override
    public int size( ) {
        return listeners.size( );
    }

    @RequiredArgsConstructor
    private final class UnmodifiableIterator extends ForwardingIterator<T> {
        private final Iterator<T> delegate;

        @Override
        protected Iterator<T> delegate( ) {
            return delegate;
        }

        @Override
        public void remove( ) {
            throw new UnsupportedOperationException( );
        }

    }
}
