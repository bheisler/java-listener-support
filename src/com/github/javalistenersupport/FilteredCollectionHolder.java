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
