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

import org.acegisecurity.AccessDecisionManager;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.PlexusTestCase;

/**
 * Test for {@link ContinuumSecurityAspect}
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public abstract class AbstractContinuumSecurityAspectTest
    extends PlexusTestCase
{
    public static final String USERNAME = "marissa";

    private ContinuumStub continuum;

    public void setContinuum( ContinuumStub continuum )
    {
        this.continuum = continuum;
    }

    public ContinuumStub getContinuum()
    {
        return continuum;
    }

    public ContinuumSecurityAspect getAspect()
    {
        return ContinuumSecurityAspect.aspectOf();
    }

    /**
     * Get an {@link Authentication} with provided role object that can
     * be used with the chosen {@link AccessDecisionManager} 
     * 
     * @param role eg. <code>ADMIN</code>, <code>USER</code>, ...
     * @return
     */
    protected Authentication getAuthentication( String role )
    {
        return new TestingAuthenticationToken( USERNAME, "koala",
                                               new GrantedAuthority[] { new GrantedAuthorityImpl( "MOCK_" + role ) } );
    }

    protected void tearDown()
        throws Exception
    {
        SecurityContextHolder.getContext().setAuthentication( null );
        super.tearDown();
    }
}
