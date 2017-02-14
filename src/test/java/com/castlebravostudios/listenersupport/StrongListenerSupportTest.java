package com.castlebravostudios.listenersupport;

public class StrongListenerSupportTest extends ListenerSupportTestBase {

  @Override
  ListenerSupport<TestListener> getListenerSupport() {
    return ListenerSupport.create(TestListener.class);
  }
}
