package com.github.javalistenersupport;

import static com.github.javalistenersupport.ListenerSupport.newListenerSupport;
import java.util.ArrayList;
import java.util.List;
import com.github.javalistenersupport.ListenerFilter;
import com.github.javalistenersupport.ListenerSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class Test
{
    private static final ListenerSupport<TestListener> support = newListenerSupport( TestListener.class );
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
