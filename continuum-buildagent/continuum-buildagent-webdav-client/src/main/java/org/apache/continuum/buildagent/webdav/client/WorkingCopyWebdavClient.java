package org.apache.continuum.buildagent.webdav.client;

import org.apache.commons.io.IOUtils;
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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class WorkingCopyWebdavClient
{
    public static void main( String[] args )
        throws Exception
    {
        System.out.println( "Running webdav client.." );

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        // http scheme
        schemeRegistry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );

        HttpParams params = new BasicHttpParams();
        params.setParameter( ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30 );
        params.setParameter( ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean( 30 ) );

        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );

        ClientConnectionManager cm = new ThreadSafeClientConnManager( params, schemeRegistry );

        DefaultHttpClient httpClient = new DefaultHttpClient( cm, params );

        URL url = new URL( args[0] );
        URI uri = url.toURI();
        HttpGet httpGet = new HttpGet( uri );

        httpClient.getCredentialsProvider().setCredentials( new AuthScope( uri.getHost(), uri.getPort() ),
                                                                            new UsernamePasswordCredentials( "", args[1] ) );

        HttpHost targetHost = new HttpHost( url.getHost(), url.getPort(), url.getProtocol() );

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put( targetHost, basicAuth );

        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute( ClientContext.AUTH_CACHE, authCache );

        HttpResponse httpResponse = httpClient.execute( targetHost, httpGet, localcontext );

        System.out.println( "Response status code :: " + httpResponse.getStatusLine().getStatusCode() );

        InputStream is = IOUtils.toInputStream( EntityUtils.toString( httpResponse.getEntity(),
                                                                      EntityUtils.getContentCharSet( httpResponse.getEntity() ) ) );
        String content = IOUtils.toString( is );

        System.out.println( "Content :: " + content );
    }

}
