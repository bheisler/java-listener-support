package com.github.javalistenersupport;

/**
 * This exception will be thrown by the ListenerSupport classes in the unlikely
 * event that a proxying operation fails.
 *
 */
public class ProxyException extends RuntimeException
{

    public ProxyException( )
    {
        super( );
    }

    public ProxyException( String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }

    public ProxyException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ProxyException( String message )
    {
        super( message );
    }

    public ProxyException( Throwable cause )
    {
        super( cause );
    }

}
