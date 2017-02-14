package com.castlebravostudios.listenersupport;

import org.junit.Test;
import org.mockito.Mockito;

public class WeakListenerSupportTest extends ListenerSupportTestBase {

    @Override
    ListenerSupport<TestListener> getListenerSupport( ) {
        return ListenerSupport.createWeak( TestListener.class );
    }

    @Test
    public void testListenerCanBeCollected( ) throws Exception {
        TestListener l = Mockito.mock( TestListener.class );
        support.registerListener( l );
        l = null;

        while( support.size() > 0 ) {
            System.gc( );
        }
    }
}
