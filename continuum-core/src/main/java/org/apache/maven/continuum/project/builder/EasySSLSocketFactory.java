package org.apache.maven.continuum.project.builder;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

/**
 * This socket factory will create ssl socket that accepts self signed certificate
 *
 * @author olamy
 * @since 1.2.3
 */
public class EasySSLSocketFactory
    implements SocketFactory, LayeredSocketFactory
{
    private static final Logger log = LoggerFactory.getLogger( EasySSLSocketFactory.class );

    private SSLContext sslcontext = null;

    private static SSLContext createEasySSLContext()
        throws IOException
    {
        try
        {
            SSLContext context = SSLContext.getInstance( "SSL" );
            context.init( null, new TrustManager[]{new EasyX509TrustManager( null )}, null );
            return context;
        }
        catch ( Exception e )
        {
            LoggerFactory.getLogger( EasySSLSocketFactory.class ).error( e.getMessage(), e );
            throw new IOException( e.getMessage() );
        }
    }

    private SSLContext getSSLContext()
        throws IOException
    {
        if ( this.sslcontext == null )
        {
            this.sslcontext = createEasySSLContext();
        }
        return this.sslcontext;
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket, java.lang.String, int, java.net.InetAddress, int, org.apache.http.params.HttpParams)
     */
    public Socket connectSocket( Socket sock, String host, int port, InetAddress localAddress, int localPort,
                                 HttpParams params )
        throws IOException, UnknownHostException, ConnectTimeoutException
    {
        int connTimeout = HttpConnectionParams.getConnectionTimeout( params );
        int soTimeout = HttpConnectionParams.getSoTimeout( params );

        InetSocketAddress remoteAddress = new InetSocketAddress( host, port );
        SSLSocket sslsock = (SSLSocket) ( ( sock != null ) ? sock : createSocket() );

        if ( ( localAddress != null ) || ( localPort > 0 ) )
        {
            // we need to bind explicitly
            if ( localPort < 0 )
            {
                localPort = 0; // indicates "any"
            }
            InetSocketAddress isa = new InetSocketAddress( localAddress, localPort );
            sslsock.bind( isa );
        }

        sslsock.connect( remoteAddress, connTimeout );
        sslsock.setSoTimeout( soTimeout );
        return sslsock;

    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
     */
    public Socket createSocket()
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "create socket" );
        }
        return getSSLContext().getSocketFactory().createSocket();
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
     */
    public boolean isSecure( Socket socket )
        throws IllegalArgumentException
    {
        return true;
    }

    /**
     * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
     */
    public Socket createSocket( Socket socket, String host, int port, boolean autoClose )
        throws IOException, UnknownHostException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "create socket host " + host + ", port " + port );
        }
        return getSSLContext().getSocketFactory().createSocket();
    }

    // -------------------------------------------------------------------
    //  javadoc in org.apache.http.conn.scheme.SocketFactory says :
    //  Both Object.equals() and Object.hashCode() must be overridden 
    //  for the correct operation of some connection managers
    // -------------------------------------------------------------------

    public boolean equals( Object obj )
    {
        return ( ( obj != null ) && obj.getClass().equals( EasySSLSocketFactory.class ) );
    }

    public int hashCode()
    {
        return EasySSLSocketFactory.class.hashCode();
    }


}
