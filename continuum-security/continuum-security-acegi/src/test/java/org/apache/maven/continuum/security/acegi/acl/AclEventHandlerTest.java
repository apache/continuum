package org.apache.maven.continuum.security.acegi.acl;

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
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.acl.basic.BasicAclEntry;
import org.acegisecurity.acl.basic.SimpleAclEntry;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.user.model.InstancePermissions;
import org.codehaus.plexus.PlexusTestCase;

/**
 * Test for {@link AclEventHandler}.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AclEventHandlerTest
    extends PlexusTestCase
{

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    public void testAcls()
        throws Exception
    {
//        lookup( AclInitializer.ROLE );
//        AclEventHandler eventHandler = (AclEventHandler) lookup( AclEventHandler.ROLE );
//        
//
//        ProjectGroup projectGroup = new ProjectGroup();
//        projectGroup.setId( 1 );
//
//        eventHandler.getUsersInstancePermissions( projectGroup.getClass(), new Integer( projectGroup.getId()), );
//        if ( acls != null )
//        {
//            eventHandler.afterDeleteProjectGroup( projectGroup.getId() );
//        }
//
//        String user1 = "user1";
//        setUser( user1 );
//
//        eventHandler.createNewProjectGroupACL( projectGroup );
//
//        String user2 = "user2";
//        setUser( user2 );
//
//        /* set permissions to create for user 2 */
//        eventHandler.setProjectGroupPermissions( projectGroup.getId(), user2, SimpleAclEntry.CREATE );
//
//        SimpleAclEntry acl = (SimpleAclEntry) eventHandler.getProjectGroupAcl( projectGroup.getId(), user2 );
//        assertEquals( SimpleAclEntry.CREATE, acl.getMask() );
//
//        /* set permissions to delete for user 2 */
//        eventHandler.setProjectGroupPermissions( projectGroup.getId(), user2, SimpleAclEntry.DELETE );
//
//        acl = (SimpleAclEntry) eventHandler.getProjectGroupAcl( projectGroup.getId(), user2 );
//        assertEquals( SimpleAclEntry.DELETE, acl.getMask() );
//
//        Project project = new Project();
//        project.setId( 1 );
//        eventHandler.createNewProjectACL( project, projectGroup );
//
//        acls = eventHandler.getProjectGroupAcls( projectGroup.getId() );
//
//        assertEquals( "Wrong number of ACLs for ProjectGroup", 2, acls.length );
//
//        for ( int i = 0; i < acls.length; i++ )
//        {
//            acl = (SimpleAclEntry) acls[i];
//            System.out.println( acl.getRecipient() + " - " + acl.printPermissionsBlock() );
//        }
//
//        /* check that user that created ProjectGroup keeps its admin permission */
//        acl = (SimpleAclEntry) eventHandler.getProjectGroupAcl( projectGroup.getId(), user1 );
//        assertEquals( SimpleAclEntry.ADMINISTRATION, acl.getMask() );
    }

//    private InstancePermissions createInstancePermissions(String username)
//    {
//        InstancePermissions p = new InstancePermissions();
//        User u = new User();
//    }

    private void setUser( String username )
    {
        UserDetails userDetails = new User( username, "", true, true, true, true, new GrantedAuthority[0] );
        SecurityContextHolder.getContext().setAuthentication(
                                                              new UsernamePasswordAuthenticationToken( userDetails,
                                                                                                       null, null ) );
    }
}
