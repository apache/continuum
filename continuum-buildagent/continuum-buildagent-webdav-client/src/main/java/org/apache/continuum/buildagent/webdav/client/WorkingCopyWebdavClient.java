package org.apache.continuum.buildagent.webdav.client;

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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class WorkingCopyWebdavClient
{
    public static void main( String[] args )
        throws Exception
    {
        System.out.println( "Running webdav client.." );

        // get resource
        getResourceUsingHttpclient( args[0], "", args[1] );

        // list resources
        getResourcesUsingJackrabbit( args[0], "", args[1] );
    }

    private static void getResourceUsingHttpclient( String resourceUrl, String username, String password )
        throws URISyntaxException, IOException
    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );

        HttpParams params = new BasicHttpParams();
        params.setParameter( ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30 );
        params.setParameter( ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean( 30 ) );

        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );

        ClientConnectionManager cm = new ThreadSafeClientConnManager( params, schemeRegistry );

        DefaultHttpClient httpClient = new DefaultHttpClient( cm, params );

        URL url = new URL( resourceUrl );
        URI uri = url.toURI();
        HttpGet httpGet = new HttpGet( uri );

        httpClient.getCredentialsProvider().setCredentials( new AuthScope( uri.getHost(), uri.getPort() ),
                                                new UsernamePasswordCredentials( username, password ) );

        HttpHost targetHost = new HttpHost( url.getHost(), url.getPort(), url.getProtocol() );

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put( targetHost, basicAuth );

        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute( ClientContext.AUTH_CACHE, authCache );

        System.out.println( "Retrieving resource '" + url.toString() + "' using HttpClient's get method.." );

        HttpResponse httpResponse = httpClient.execute( targetHost, httpGet, localcontext );

        System.out.println( "Response status code :: " + httpResponse.getStatusLine().getStatusCode() );

        InputStream is = IOUtils.toInputStream( EntityUtils.toString( httpResponse.getEntity(),
                                                                      EntityUtils.getContentCharSet( httpResponse.getEntity() ) ) );
        String content = IOUtils.toString( is );

        System.out.println( "Content :: " + content );
    }

    private static void getResourcesUsingJackrabbit( String filePath, String username, String password )
        throws IOException, URISyntaxException, DavException
    {
        int idx = filePath.lastIndexOf( "/" );
        if( idx != -1 )
        {
            filePath = StringUtils.substring( filePath, 0, idx + 1 );
        }

        System.out.println( "\nRetrieve resources from '" + filePath + "' using Jackrabbit's webdav client.." );

        URL url = new URL( filePath );
        URI uri = url.toURI();

        DavMethod pFind = new PropFindMethod( uri.toString(), DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1 );

        executeMethod( username, password, uri, pFind );

        MultiStatus multiStatus = pFind.getResponseBodyAsMultiStatus();
        MultiStatusResponse[] responses = multiStatus.getResponses();
        MultiStatusResponse currResponse;
        System.out.println("Folders and files in " + filePath + ":");

        for( int i = 0; i < responses.length; i++ )
        {
            currResponse = responses[i];
            if ( !( currResponse.getHref().equals( uri.toString() ) || currResponse.getHref().equals( uri.toString() + "/") ) )
            {
                String currResponseHref = StringUtils.trim( currResponse.getHref() );

                System.out.println( "\nResource url :: " + currResponseHref );

                DavProperty displayNameDavProperty = currResponse.getProperties( HttpStatus.SC_OK ).get( "displayname" );
                String displayName;
                if( displayNameDavProperty != null )
                {
                    displayName = (String) displayNameDavProperty.getValue();
                }
                else
                {
                    displayName = StringUtils.substring( currResponseHref, currResponseHref.lastIndexOf( "/" ) );
                }

                HttpMethod httpGet = new GetMethod( currResponseHref );

                URL resourceUrl = new URL( currResponseHref );

                executeMethod( username, password, resourceUrl.toURI(), httpGet );

                System.out.println( "Returned status code :: " + httpGet.getStatusCode() );

                if( httpGet.getStatusCode() == HttpStatus.SC_OK )
                {
                    InputStream is = httpGet.getResponseBodyAsStream();

                    try
                    {
                        System.out.println( "Contents of file '" + displayName + "' :: " + IOUtils.toString( is ) );
                    }
                    finally
                    {
                        IOUtils.closeQuietly( is );
                    }
                }
            }
        }
    }

    private static void executeMethod( String username, String password, URI uri, HttpMethod pFind )
        throws IOException
    {
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost( uri.getHost() );

        int maxHostConnections = 20;

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);

        HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setParams(params);

        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient(connectionManager);

        Credentials creds = new org.apache.commons.httpclient.UsernamePasswordCredentials( username, password );

        client.getState().setCredentials(org.apache.commons.httpclient.auth.AuthScope.ANY, creds);
        client.setHostConfiguration(hostConfig);
        client.getParams().setAuthenticationPreemptive( true );

        client.executeMethod( hostConfig, pFind );
    }

}
