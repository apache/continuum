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

import org.apache.commons.io.IOUtils;
import org.apache.continuum.utils.file.FileSystemManager;
import org.apache.http.HttpException;
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
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractContinuumProjectBuilder
    implements ContinuumProjectBuilder, Initializable
{
    protected final Logger log = LoggerFactory.getLogger( getClass() );

    @Requirement
    protected FileSystemManager fsManager;

    private HttpParams params;

    private ClientConnectionManager cm;

    public void initialize()
        throws InitializationException
    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        // http scheme
        schemeRegistry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );
        // https scheme
        schemeRegistry.register( new Scheme( "https", new EasySSLSocketFactory(), 443 ) );

        params = new BasicHttpParams();
        // TODO put this values to a configuration way ???
        params.setParameter( ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30 );
        params.setParameter( ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean( 30 ) );
        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );

        cm = new ThreadSafeClientConnManager( params, schemeRegistry );
    }

    protected File createMetadataFile( File importRoot, URL metadata, String username, String password,
                                       ContinuumProjectBuildingResult result )
        throws IOException, URISyntaxException, HttpException
    {
        DefaultHttpClient httpClient = new DefaultHttpClient( cm, params );

        String url = metadata.toExternalForm();
        if ( metadata.getProtocol().startsWith( "http" ) )
        {
            url = hidePasswordInUrl( url );
        }
        log.info( "Downloading " + url );

        InputStream is = null;
        try
        {

            if ( metadata.getProtocol().startsWith( "http" ) )
            {
                URI uri = metadata.toURI();
                HttpGet httpGet = new HttpGet( uri );

                httpClient.getCredentialsProvider().clear();

                // basic auth
                if ( username != null && password != null )
                {
                    httpClient.getCredentialsProvider().setCredentials( new AuthScope( uri.getHost(), uri.getPort() ),
                                                                        new UsernamePasswordCredentials( username,
                                                                                                         password ) );
                }

                // basic auth
                HttpResponse httpResponse = httpClient.execute( httpGet );

                // CONTINUUM-2627
                if ( httpResponse.getStatusLine().getStatusCode() != 200 )
                {
                    log.debug(
                        "Initial attempt did not return a 200 status code. Trying pre-emptive authentication.." );

                    HttpHost targetHost = new HttpHost( uri.getHost(), uri.getPort(), uri.getScheme() );

                    // Create AuthCache instance
                    AuthCache authCache = new BasicAuthCache();
                    // Generate BASIC scheme object and add it to the local auth cache
                    BasicScheme basicAuth = new BasicScheme();
                    authCache.put( targetHost, basicAuth );

                    // Add AuthCache to the execution context
                    BasicHttpContext localcontext = new BasicHttpContext();
                    localcontext.setAttribute( ClientContext.AUTH_CACHE, authCache );

                    httpResponse = httpClient.execute( targetHost, httpGet, localcontext );
                }

                int res = httpResponse.getStatusLine().getStatusCode();

                switch ( res )
                {
                    case 200:
                        break;
                    case 401:
                        log.error( "Error adding project: Unauthorized " + url );
                        result.addError( ContinuumProjectBuildingResult.ERROR_UNAUTHORIZED );
                        return null;
                    default:
                        log.warn( "skip non handled http return code " + res );
                }
                is = IOUtils.toInputStream(
                    EntityUtils.toString( httpResponse.getEntity(), EntityUtils.getContentCharSet(
                        httpResponse.getEntity() ) ) );
            }
            else
            {
                is = metadata.openStream();
            }

            String path = metadata.getPath(), baseDirectory, fileName;

            // Split the URL's path into base directory and filename
            int lastIndex = path.lastIndexOf( "/" );
            if ( lastIndex >= 0 )
            {
                baseDirectory = path.substring( 0, lastIndex );
                // Required for windows
                int colonIndex = baseDirectory.indexOf( ":" );
                if ( colonIndex >= 0 )
                {
                    baseDirectory = baseDirectory.substring( colonIndex + 1 );
                }
                fileName = path.substring( lastIndex + 1 );
            }
            else
            {
                baseDirectory = "";
                fileName = path;
            }

            // Hack for URLs containing '*' like "http://svn.codehaus.org/*checkout*/trunk/pom.xml?root=plexus"
            baseDirectory = baseDirectory.replaceAll( "[*]", "" );
            File uploadDirectory = new File( importRoot, baseDirectory );

            // Re-create the directory structure as existed remotely if necessary
            uploadDirectory.mkdirs();

            // Write the metadata file (with the same name, like pom.xml)
            File file = new File( uploadDirectory, fileName );
            fsManager.writeFile( file, is );

            return file;
        }
        finally
        {
            if ( is != null )
            {
                is.close();
            }
        }
    }

    private String hidePasswordInUrl( String url )
    {
        int indexAt = url.indexOf( "@" );

        if ( indexAt < 0 )
        {
            return url;
        }

        String s = url.substring( 0, indexAt );

        int pos = s.lastIndexOf( ":" );

        return s.substring( 0, pos + 1 ) + "*****" + url.substring( indexAt );
    }

    /**
     * Create metadata file and handle exceptions, adding the errors to the result object.
     *
     * @param result   holder with result and errors.
     * @param metadata
     * @param username
     * @param password
     * @return
     */
    protected File createMetadataFile( File importRoot, ContinuumProjectBuildingResult result, URL metadata,
                                       String username, String password )
    {
        String url = metadata.toExternalForm();

        if ( metadata.getProtocol().startsWith( "http" ) )
        {
            url = hidePasswordInUrl( url );
        }

        try
        {
            return createMetadataFile( importRoot, metadata, username, password, result );
        }
        catch ( FileNotFoundException e )
        {
            log.info( "Metadata creation failed for '{}': {}", url, e.getMessage() );
            result.addError( ContinuumProjectBuildingResult.ERROR_POM_NOT_FOUND );
        }
        catch ( MalformedURLException e )
        {
            log.info( "Malformed URL: " + url, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }
        catch ( URISyntaxException e )
        {
            log.info( "Malformed URL: " + url, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }
        catch ( UnknownHostException e )
        {
            log.info( "Unknown host: " + url, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN_HOST );
        }
        catch ( IOException e )
        {
            log.warn( "Could not download the URL: " + url, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );
        }
        catch ( HttpException e )
        {
            log.warn( "Could not download the URL: " + url, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );
        }
        return null;
    }

}
