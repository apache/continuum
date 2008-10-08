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
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;


/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumProjectBuilder
    extends AbstractLogEnabled
    implements ContinuumProjectBuilder, Initializable
{

    private static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );

    private DefaultHttpClient httpClient;


    public void initialize()
        throws InitializationException
    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        // http scheme
        schemeRegistry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );
        // https scheme
        SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();

        // ignore cert
        sslSocketFactory.setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
        schemeRegistry.register( new Scheme( "https", sslSocketFactory, 443 ) );

        HttpParams params = new BasicHttpParams();
        // TODO put this values to a configuration way ???
        params.setParameter( ConnManagerPNames.MAX_TOTAL_CONNECTIONS, new Integer( 30 ) );
        params.setParameter( ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean( 30 ) );
        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );

        ClientConnectionManager cm = new ThreadSafeClientConnManager( params, schemeRegistry );

        httpClient = new DefaultHttpClient( cm, params );


    }

    protected File createMetadataFile( URL metadata, String username, String password,
                                       ContinuumProjectBuildingResult result )
        throws IOException, URISyntaxException, HttpException
    {
        String url = metadata.toExternalForm();
        if ( metadata.getProtocol().startsWith( "http" ) )
        {
            url = hidePasswordInUrl( url );
        }
        getLogger().info( "Downloading " + url );

        InputStream is = null;

        if ( metadata.getProtocol().startsWith( "http" ) )
        {
            URI uri = metadata.toURI();
            HttpGet httpGet = new HttpGet( uri );

            // basic auth
            if ( username != null && password != null )
            {
                httpClient.getCredentialsProvider()
                    .setCredentials( new AuthScope( uri.getHost(), uri.getPort() ),
                                     new UsernamePasswordCredentials( username, password ) );
            }

            HttpResponse httpResponse = httpClient.execute( httpGet );

            // basic auth 

            int res = httpResponse.getStatusLine().getStatusCode();
            switch ( res )
            {
                case 200:
                    break;
                case 401:
                    getLogger().error( "Error adding project: Unauthorized " + metadata, null );
                    result.addError( ContinuumProjectBuildingResult.ERROR_UNAUTHORIZED );
                    return null;
                default:
                    getLogger().warn( "skip non handled http return code " + res );
            }
            is = IOUtils.toInputStream( EntityUtils.toString( httpResponse.getEntity(), EntityUtils
                .getContentCharSet( httpResponse.getEntity() ) ) );
        }
        else
        {
            is = metadata.openStream();
        }

        String path = metadata.getPath();

        String baseDirectory;

        String fileName;

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

        // Little hack for URLs that contains '*' like "http://svn.codehaus.org/*checkout*/trunk/pom.xml?root=plexus"
        baseDirectory = StringUtils.replace( baseDirectory, "*", "" );

        File continuumTmpDir = new File( TMP_DIR, "continuum" );

        // FIXME should deleted after has been reading
        File uploadDirectory = new File( continuumTmpDir, baseDirectory );

        uploadDirectory.deleteOnExit();

        // resolve any '..' as it will cause issues
        uploadDirectory = uploadDirectory.getCanonicalFile();

        uploadDirectory.mkdirs();

        FileUtils.forceDeleteOnExit( continuumTmpDir );

        File file = new File( uploadDirectory, fileName );

        file.deleteOnExit();

        FileWriter writer = new FileWriter( file );

        IOUtil.copy( is, writer );

        is.close();

        writer.close();

        return file;
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
    protected File createMetadataFile( ContinuumProjectBuildingResult result, URL metadata, String username,
                                       String password )
    {
        try
        {
            return createMetadataFile( metadata, username, password, result );
        }
        catch ( FileNotFoundException e )
        {
            getLogger().info( "URL not found: " + metadata, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_POM_NOT_FOUND );
        }
        catch ( MalformedURLException e )
        {
            getLogger().info( "Malformed URL: " + metadata, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }
        catch ( URISyntaxException e )
        {
            getLogger().info( "Malformed URL: " + metadata, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }
        catch ( UnknownHostException e )
        {
            getLogger().info( "Unknown host: " + metadata, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN_HOST );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not download the URL: " + metadata, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );
        }
        catch ( HttpException e )
        {
            getLogger().warn( "Could not download the URL: " + metadata, e );
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );
        }
        return null;
    }

}
