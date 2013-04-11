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
import lombok.RequiredArgsConstructor;
import com.google.common.collect.AbstractIterator;

/**
 * A collection holder that delegates iteration to a given CollectionHolder, but
 * filters the results with a given ListenerFilter. Registering and Unregistering
 * listeners on a FilteredCollectionHolder is not permitted.
 */
@RequiredArgsConstructor
class FilteredCollectionHolder<T> implements CollectionHolder<T>
{

    private final CollectionHolder<T> base;
    private final ListenerFilter<T> filter;

    @Override
    public Iterator<T> iterator( )
    {
        return new FilteredIterator( base.iterator( ), filter );
    }

    @Override
    public void registerListener( T listener )
    {
        throw new UnsupportedOperationException( "Can't insert elements into a filtered collection holder. ");
    }

    @Override
    public void unregisterListener( T listener )
    {
        throw new UnsupportedOperationException( "Can't insert elements into a filtered collection holder. ");
    }

    @RequiredArgsConstructor
    private class FilteredIterator extends AbstractIterator<T> {

        private final Iterator<T> base;
        private final ListenerFilter<T> filter;

        @Override
        protected T computeNext( )
        {
            while ( base.hasNext( ) ) {
                T listener = base.next( );
                if ( filter.passesFilter( listener ) ) {
                    return listener;
                }
            }
            return endOfData( );
        }

    }
}
