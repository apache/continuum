package org.apache.maven.continuum.security.acegi.aspectj;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.io.InputStreamReader;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;

public class MethodSecurityPlexusTest
    extends AbstractMethodSecurityTest
{

    private PlexusContainer pc;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream( "org/apache/maven/continuum/security/acegi/aspectj/MethodSecurityPlexusTest.xml" );

        pc = new DefaultPlexusContainer();
        pc.setConfigurationResource( new InputStreamReader( is ) );
        pc.initialize();
        pc.start();
        setContinuum( (Continuum) pc.lookup( "org.apache.maven.continuum.Continuum" ) );
    }

    protected Authentication getAuthentication( String role )
    {
        return new UsernamePasswordAuthenticationToken( "marissa", "koala",
                                                        new GrantedAuthority[] { new GrantedAuthorityImpl( "ROLE_"
                                                            + role ) } );
    }

    protected void tearDown()
        throws Exception
    {
        pc.dispose();
        super.tearDown();
    }
}
