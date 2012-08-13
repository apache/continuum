package org.apache.maven.continuum.xmlrpc.server;

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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeConverter;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
import org.apache.xmlrpc.metadata.Util;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ContinuumXmlRpcMetaDataHandler
    implements XmlRpcHandler
{
    private static class MethodData
    {
        final Method method;

        final TypeConverter[] typeConverters;

        MethodData( Method pMethod, TypeConverterFactory pTypeConverterFactory )
        {
            method = pMethod;
            Class[] paramClasses = method.getParameterTypes();
            typeConverters = new TypeConverter[paramClasses.length];
            for ( int i = 0; i < paramClasses.length; i++ )
            {
                typeConverters[i] = pTypeConverterFactory.getTypeConverter( paramClasses[i] );
            }
        }
    }

    private final AbstractReflectiveHandlerMapping mapping;

    private final MethodData[] methods;

    private final Class clazz;

    private final RequestProcessorFactoryFactory.RequestProcessorFactory requestProcessorFactory;

    private final String[][] signatures;

    private final String methodHelp;

    private final PlexusContainer container;

    /**
     * Creates a new instance.
     *
     * @param pMapping   The mapping, which creates this handler.
     * @param pClass     The class, which has been inspected to create
     *                   this handler. Typically, this will be the same as
     *                   <pre>pInstance.getClass()</pre>. It is used for diagnostic
     *                   messages only.
     * @param pMethods   The method, which will be invoked for
     *                   executing the handler.
     * @param signatures The signature, which will be returned by
     *                   {@link #getSignatures()}.
     * @param methodHelp The help string, which will be returned
     *                   by {@link #getMethodHelp()}.
     * @param container  The container that loaded the component
     */
    public ContinuumXmlRpcMetaDataHandler( AbstractReflectiveHandlerMapping pMapping,
                                           TypeConverterFactory pTypeConverterFactory, Class pClass,
                                           RequestProcessorFactoryFactory.RequestProcessorFactory pFactory,
                                           Method[] pMethods, String[][] signatures, String methodHelp,
                                           PlexusContainer container )
    {
        mapping = pMapping;
        clazz = pClass;
        methods = new MethodData[pMethods.length];
        requestProcessorFactory = pFactory;
        for ( int i = 0; i < methods.length; i++ )
        {
            methods[i] = new MethodData( pMethods[i], pTypeConverterFactory );
        }
        this.signatures = signatures;
        this.methodHelp = methodHelp;
        this.container = container;
    }

    private Object getInstance( XmlRpcRequest pRequest )
        throws XmlRpcException
    {
        return requestProcessorFactory.getRequestProcessor( pRequest );
    }

    public Object execute( XmlRpcRequest pRequest )
        throws XmlRpcException
    {
        AbstractReflectiveHandlerMapping.AuthenticationHandler authHandler = mapping.getAuthenticationHandler();
        if ( authHandler != null && !authHandler.isAuthorized( pRequest ) )
        {
            throw new XmlRpcNotAuthorizedException( "Not authorized" );
        }
        Object[] args = new Object[pRequest.getParameterCount()];
        for ( int j = 0; j < args.length; j++ )
        {
            args[j] = pRequest.getParameter( j );
        }
        Object instance = getInstance( pRequest );
        for ( MethodData methodData : methods )
        {
            TypeConverter[] converters = methodData.typeConverters;
            if ( args.length == converters.length )
            {
                boolean matching = true;
                for ( int j = 0; j < args.length; j++ )
                {
                    if ( !converters[j].isConvertable( args[j] ) )
                    {
                        matching = false;
                        break;
                    }
                }
                if ( matching )
                {
                    for ( int j = 0; j < args.length; j++ )
                    {
                        args[j] = converters[j].convert( args[j] );
                    }
                    return invoke( instance, methodData.method, args );
                }
            }
        }
        throw new XmlRpcException( "No method matching arguments: " + Util.getSignature( args ) );
    }

    private Object invoke( Object pInstance, Method pMethod, Object[] pArgs )
        throws XmlRpcException
    {
        try
        {
            return pMethod.invoke( pInstance, pArgs );
        }
        catch ( IllegalAccessException e )
        {
            throw new XmlRpcException( "Illegal access to method " + pMethod.getName() + " in class " + clazz.getName(),
                                       e );
        }
        catch ( IllegalArgumentException e )
        {
            throw new XmlRpcException(
                "Illegal argument for method " + pMethod.getName() + " in class " + clazz.getName(), e );
        }
        catch ( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            if ( t instanceof XmlRpcException )
            {
                throw (XmlRpcException) t;
            }
            throw new XmlRpcException(
                "Failed to invoke method " + pMethod.getName() + " in class " + clazz.getName() + ": " + t.getMessage(),
                t );
        }
        finally
        {
            try
            {
                container.release( pInstance );
            }
            catch ( ComponentLifecycleException e )
            {
                //Do nothing
            }
        }
    }

    public String[][] getSignatures()
        throws XmlRpcException
    {
        return signatures;
    }

    public String getMethodHelp()
        throws XmlRpcException
    {
        return methodHelp;
    }

}
