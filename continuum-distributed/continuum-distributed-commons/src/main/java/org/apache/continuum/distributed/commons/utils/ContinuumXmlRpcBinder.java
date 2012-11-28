package org.apache.continuum.distributed.commons.utils;

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

import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.BinderTypeFactory;
import com.atlassian.xmlrpc.BindingException;
import com.atlassian.xmlrpc.ConnectionInfo;
import com.atlassian.xmlrpc.ServiceObject;
import com.atlassian.xmlrpc.XmlRpcClientProvider;
import com.atlassian.xmlrpc.XmlRpcInvocationHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Vector;

/**
 * Used to bind the given XML-RPC service to an instance of
 * the given interface type.
 *
 * This implementation uses the Apache XML-RPC client.
 *
 * This is derived from ApacheBinder from the atlassian-xmlrpc-binder project. In this version, we customise the
 * transport used for better connection management. It is thread-safe and should be reused.
 *
 * @author <a href="mailto:james@atlassian.com">James William Dumay</a>
 */
public class ContinuumXmlRpcBinder
    implements Binder
{
    private final HttpClient httpClient;

    private static ContinuumXmlRpcBinder binder = new ContinuumXmlRpcBinder();

    private ContinuumXmlRpcBinder()
    {
        this.httpClient = new HttpClient( new MultiThreadedHttpConnectionManager() );
    }

    public <T> T bind( Class<T> bindClass, URL url )
        throws BindingException
    {
        return bind( bindClass, url, new ConnectionInfo() );
    }

    public <T> T bind( Class<T> bindClass, URL url, ConnectionInfo connectionInfo )
        throws BindingException
    {
        if ( !bindClass.isInterface() )
        {
            throw new BindingException( "Class " + bindClass.getName() + "is not an interface" );
        }
        ServiceObject serviceObject = bindClass.getAnnotation( ServiceObject.class );
        if ( serviceObject == null )
        {
            throw new BindingException( "Could not find ServiceObject annotation on " + bindClass.getName() );
        }
        final XmlRpcClient client = getXmlRpcClient( url, connectionInfo );

        XmlRpcInvocationHandler handler = new XmlRpcInvocationHandler( new XmlRpcClientProvider()
        {
            public Object execute( String serviceName, String methodName, Vector arguments )
                throws BindingException
            {
                try
                {
                    return client.execute( serviceName + "." + methodName, arguments );
                }
                catch ( XmlRpcException e )
                {
                    throw new BindingException( e );
                }
            }
        } );

        return (T) Proxy.newProxyInstance( getClass().getClassLoader(), new Class[]{bindClass}, handler );
    }

    private XmlRpcClient getXmlRpcClient( URL url, ConnectionInfo connectionInfo )
    {
        XmlRpcClientConfigImpl clientConfig = new XmlRpcClientConfigImpl();
        clientConfig.setServerURL( url );
        clientConfig.setEnabledForExceptions( true );

        if ( connectionInfo != null )
        {
            clientConfig.setBasicUserName( connectionInfo.getUsername() );
            clientConfig.setBasicPassword( connectionInfo.getPassword() );
            clientConfig.setBasicEncoding( connectionInfo.getEncoding() );
            clientConfig.setGzipCompressing( connectionInfo.isGzip() );
            clientConfig.setGzipRequesting( connectionInfo.isGzip() );
            clientConfig.setReplyTimeout( connectionInfo.getTimeout() );
            clientConfig.setConnectionTimeout( connectionInfo.getTimeout() );
            clientConfig.setTimeZone( connectionInfo.getTimeZone() );
        }

        final XmlRpcClient client = new XmlRpcClient();
        client.setTypeFactory( new BinderTypeFactory( client ) );
        XmlRpcCommonsTransportFactory factory = new XmlRpcCommonsTransportFactory( client );
        // Alternative - use simple connection manager, but make sure it closes the connection each time
        // This would be set here since it would not be thread-safe
//        factory.setHttpClient( new HttpClient( new SimpleHttpConnectionManager( true ) ) );
        factory.setHttpClient( httpClient );
        client.setConfig( clientConfig );
        return client;
    }

    public static ContinuumXmlRpcBinder getInstance()
    {
        return binder;
    }
}
