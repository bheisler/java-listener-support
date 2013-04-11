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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.RequiredArgsConstructor;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

/**
 * A collection holder that holds a set of WeakReferences to listeners.
 * The iterator returned from iterator() is guaranteed
 * to never return a null reference. Clients may not insert a null
 * reference. The collection is cleaned of dead references before
 * returning from iterator().
 * <br>
 * This class is thread-safe.
 */
class WeakCollectionHolder<T> implements CollectionHolder<T>
{

    private final CopyOnWriteArraySet<WeakReference<T>> listeners = new CopyOnWriteArraySet<>( );

    private final ReferenceQueue<T> referenceQueue = new ReferenceQueue<>( );

    @Override
    public void registerListener( T listener )
    {
        Preconditions.checkNotNull( listener );
        listeners.add( new WeakReference<T>( listener, referenceQueue ) );
    }

    @Override
    public void unregisterListener( T listener )
    {
        for ( WeakReference<T> ref : listeners ) {
            if ( listener == ref.get( ) ) {
                listeners.remove( ref );
                break;
            }
        }
    }

    private void cleanup() {
        List<Reference<?>> references = new ArrayList<>();
        Reference<?> ref = referenceQueue.poll( );
        while( ref != null ) {
            references.add( ref );
            ref = referenceQueue.poll( );
        }
        listeners.removeAll( references );
    }

    @Override
    public Iterator<T> iterator( )
    {
        cleanup();
        return new WeakReferenceIterator( listeners.iterator( ) );
    }

    @RequiredArgsConstructor
    private final class WeakReferenceIterator extends AbstractIterator<T> {

        private final Iterator<WeakReference<T>> base;

        @Override
        protected T computeNext( )
        {
            while ( base.hasNext( ) ) {
                WeakReference<T> ref = base.next( );
                T listener = ref.get( );
                if ( listener != null ) {
                    return listener;
                }
            }
            return endOfData( );
        }
    }

}
