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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.maven.continuum.model.project.Project;

/**
 * Test for {@link ContinuumSecurityAspect}, project instance level security
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public abstract class AbstractProjectSecurityTest
    extends AbstractContinuumSecurityAspectTest
{

    protected Authentication getAuthentication( String role )
    {
        return new UsernamePasswordAuthenticationToken( USERNAME, "koala",
                                                        new GrantedAuthority[] { new GrantedAuthorityImpl( "ROLE_"
                                                            + role ) } );
    }

    public void testGetAllProjects()
        throws Exception
    {
        Project project1 = new Project();
        project1.setId( 1 );
    
        Project project2 = new Project();
        project2.setId( 2 );
    
        List mockProjects = new ArrayList();
        mockProjects.add( project1 );
        mockProjects.add( project2 );
    
        getContinuum().setMockProjects( mockProjects );
    
        SecurityContextHolder.getContext().setAuthentication( getAuthentication( "USER" ) );
    
        Collection allProjects = getContinuum().getAllProjects( 1, 1000 );
    
        assertEquals( "Number of projects returned does not match", 1, allProjects.size() );
        assertEquals( "The returned project is not the right one", 1, ( (Project) allProjects.iterator().next() )
            .getId() );
    }

}
