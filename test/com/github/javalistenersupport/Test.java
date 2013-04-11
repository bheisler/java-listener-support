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

import java.util.ArrayList;
import java.util.List;
import com.github.javalistenersupport.ListenerFilter;
import com.github.javalistenersupport.ListenerSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class Test
{
    private static final ListenerSupport<TestListener> support = ListenerSupport.create( TestListener.class );
    private static final List<TestListener> memory = new ArrayList<>( 10000 );

    static {
        for ( int k = 0; k < 10000; k++ ) {
            TestListener listener = new TestListenerImpl( k );
            support.registerListener( listener );
            memory.add( listener );
        }
    }


    @RequiredArgsConstructor
    private static class TestListenerImpl implements TestListener {

        @Getter private final int num;

        @Override
        public void testEvent( )
        {
            System.out.println( num );
        }

    }

    public static void main( String[] args )
    {
        Test test = new Test();

        ListenerFilter<TestListener> filter = new ListenerFilter<TestListener>( ) {

            @Override
            public boolean passesFilter( TestListener listener )
            {
                if ( listener instanceof TestListenerImpl ) {
                    TestListenerImpl impl = (TestListenerImpl) listener;
                    return impl.getNum( ) % 2 == 1;
                }
                return false;
            }

        };
        support.filter( filter ).fire( ).testEvent( );
    }
}
