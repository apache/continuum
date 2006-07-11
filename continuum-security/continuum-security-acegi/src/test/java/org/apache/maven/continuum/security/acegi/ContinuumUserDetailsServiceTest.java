package org.apache.maven.continuum.security.acegi;

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

import org.acegisecurity.providers.encoding.ShaPasswordEncoder;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.model.system.UserGroup;

import junit.framework.TestCase;

/**
 * Test for {@link ContinuumUserDetailsService}
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ContinuumUserDetailsServiceTest
    extends TestCase
{

    private ContinuumUserDetailsService userDetailsService;

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    public void testGetUserDetails()
    {
        Permission p1 = new Permission();
        p1.setName( "p1" );
        Permission p2 = new Permission();
        p2.setName( "p2" );
        Permission p3 = new Permission();
        p3.setName( "p3" );
        
        UserGroup group = new UserGroup();
        group.addPermission( p1 );
        group.addPermission( p2 );
        group.addPermission( p3 );
        
        ContinuumUser continuumUser = new ContinuumUser();
        continuumUser.setUsername( "username" );
        continuumUser.setPassword( "password" );
        continuumUser.setGroup( group );
        
        ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();
        String shaPassword = passwordEncoder.encodePassword( "password", null );
        
        UserDetails userDetails = userDetailsService.getUserDetails( continuumUser );
        
        assertEquals( userDetails.getUsername(), continuumUser.getUsername() );
        assertEquals( userDetails.getPassword(), shaPassword );
        assertEquals( userDetails.getAuthorities(), continuumUser.getUsername() );
    }

    public void testPasswordEncoding()
    {
        ContinuumUser continuumUser = new ContinuumUser();
        continuumUser.setPassword( "admin" );
        
        ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();
        String shaPassword = passwordEncoder.encodePassword( "admin", null );
        
        assertEquals( continuumUser.getHashedPassword(), shaPassword );
    }
}
