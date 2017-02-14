package com.castlebravostudios.listenersupport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class ListenerSupportTestBase {

    public interface TestListener {
        void call();
    }

    @Mock
    protected TestListener listener1;

    @Mock
    protected TestListener listener2;

    protected ListenerSupport<TestListener> support;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks( this );
        support = getListenerSupport( );
    }

    @Test
    public void testCallsRegisteredListener( ) throws Exception {
        support.registerListener( listener1 );
        support.fire().call( );
        verify( listener1 ).call( );
    }

    @Test
    public void testCanCallMultipleTimes( ) throws Exception {
        support.registerListener( listener1 );
        support.fire().call( );
        support.fire().call( );
        verify( listener1, times( 2 ) ).call( );
    }

    @Test
    public void testDoesNotCallUnregisteredListener( ) throws Exception {
        support.registerListener( listener1 );
        support.unregisterListener( listener1 );
        support.fire().call( );
        verify( listener1, never() ).call( );
    }

    @Test
    public void testAssertSizeIsCorrect( ) throws Exception {
        assertEquals( 0, support.size() );
        support.registerListener( listener1 );
        assertEquals( 1, support.size() );
    }

    @Test
    public void testCanFilterListeners( ) throws Exception {
        support.registerListener( listener1 );
        support.registerListener( listener2 );

        support.filter( listener -> listener == listener1 ).fire().call();

        verify( listener1 ).call();
        verify( listener2, never() ).call( );
    }

    @Test(timeout=1000)
    public void testInvokeInParallel( ) throws Exception {
        int listenerCount = 10000;
        CountDownLatch latch = new CountDownLatch( listenerCount );
        List<TestListener> listeners = new ArrayList<>();
        for ( int k = 0; k < listenerCount; k++ ) {
            TestListener listener = () -> latch.countDown( );
            listeners.add( listener );
            support.registerListener( listener );
        }

        support.fireInParallel( ).call();

        latch.await( );
    }

    @Test
    public void testFilterGivesCorrectSize( ) throws Exception {
        support.registerListener( listener1 );
        support.registerListener( listener2 );

        assertEquals( 1, support.filter( listener -> listener == listener1 ).size() );
    }

    abstract ListenerSupport<TestListener> getListenerSupport();
}
