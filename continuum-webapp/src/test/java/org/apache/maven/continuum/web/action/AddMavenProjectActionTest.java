package org.apache.maven.continuum.web.action;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import org.apache.continuum.web.action.AbstractActionTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.web.action.stub.AddMavenProjectStub;
import org.jmock.Mock;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Verifies {@link org.apache.maven.continuum.web.action.AddMavenProjectAction}.
 */
public class AddMavenProjectActionTest
    extends AbstractActionTest
{
    private AddMavenProjectStub action;

    private Mock requestMock;

    public void setUp()
        throws Exception
    {
        super.setUp();

        action = new AddMavenProjectStub();

        // TODO: upgrade to generics-based mocking
        requestMock = new Mock( HttpServletRequest.class );
        action.setServletRequest( (HttpServletRequest) requestMock.proxy() );

    }

    public void testHttpUrlConstructionWithCreds()
        throws BuildDefinitionServiceException, ContinuumException, MalformedURLException, UnsupportedEncodingException
    {
        String scheme = "http";
        String host = "www.example.com";
        String port = "8080";
        String path = "/project/path/perhaps";
        String query = "fileName=pom.xml&project=Project%20Name";
        String username = "batkinson@apache.org";
        String password = "p&s/W:rd";
        String encoding = "UTF-8";
        String urlToFetch = String.format( "%s://%s:%s%s?%s", scheme, host, port, path, query );

        action.setPomUrl( urlToFetch );
        action.setScmUsername( username );
        action.setScmPassword( password );
        requestMock.expects( once() ).method( "getCharacterEncoding" ).will( returnValue( encoding ) );

        String result = action.execute();

        requestMock.verify();
        assertEquals( "action should have succeeded", Action.SUCCESS, result );

        URL builtUrl = new URL( action.getPom() );
        String expectedUserInfo =
            String.format( "%s:%s", URLEncoder.encode( username, encoding ), URLEncoder.encode( password, encoding ) );

        assertEquals( "url should include encoded user information", expectedUserInfo, builtUrl.getUserInfo() );
        assertEquals( "url should include original protocol scheme", scheme, builtUrl.getProtocol() );
        assertEquals( "url should include original host", host, builtUrl.getHost() );
        assertEquals( "url should include original port", port, Integer.toString( builtUrl.getPort() ) );
        assertEquals( "url should include original path ", path, builtUrl.getPath() );
        assertEquals( "url should include original query params", query, builtUrl.getQuery() );
    }
}
