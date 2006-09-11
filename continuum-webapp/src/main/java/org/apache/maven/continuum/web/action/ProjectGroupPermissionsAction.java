package org.apache.maven.continuum.web.action;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.user.model.InstancePermissions;
import org.apache.maven.user.model.User;
import org.apache.maven.user.model.UserManager;

/**
 * Action to see and edit project group permissions per user.
 *
 * @author <a href="mailto:hisidro@exist.com">Henry Isidro</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="projectGroupPermissions"
 */
public class ProjectGroupPermissionsAction
    extends ContinuumActionSupport
{
    /**
     * @plexus.requirement
     */
    private UserManager userManager;

    private ProjectGroup projectGroup;

    private int projectGroupId;

    private InstancePermissions[] userPermissions;

    private String[] userNames;

    private Map map;

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public void setUserPermissions( InstancePermissions[] userPermissions )
    {
        this.userPermissions = userPermissions;
    }

    public InstancePermissions[] getUserPermissions()
    {
        return userPermissions;
    }

    public Map getMap()
    {
        return map;
    }

    public void setMap( Map map )
    {
        this.map = map;
    }

    public void setUserNames( String[] userNames )
    {
        this.userNames = userNames;
    }

    public String[] getUserNames()
    {
        return userNames;
    }

    public String execute()
        throws ContinuumException
    {
        List userPermissionsAsList = userManager.getUsersInstancePermissions( ProjectGroup.class,
                                                                              new Integer( projectGroupId ) );

        setUserPermissions( (InstancePermissions[]) userPermissionsAsList.toArray( new InstancePermissions[0] ) );

        setProjectGroup( getContinuum().getProjectGroup( projectGroupId ) );

        return INPUT;
    }

    public String save()
        throws ContinuumException
    {
        List instancePermissions = new ArrayList( userNames.length );
        for ( int i = 0; i < userNames.length; i++ )
        {
            User u = new User();
            u.setUsername( userNames[i] );

            InstancePermissions p = parsePermissions( map, u.getUsername() );
            p.setUser( u );
            p.setId( new Integer( projectGroupId ) );
            p.setInstanceClass( ProjectGroup.class );
            instancePermissions.add( p );

            // TODO validate that the permissions dont conflict each other, set the error msg if they do
        }
        userManager.setUsersInstancePermissions( instancePermissions );

        return SUCCESS;
    }

    private InstancePermissions parsePermissions( Map map, String username )
    {
        InstancePermissions p = new InstancePermissions();
        if ( map.get( username + ".execute" ) != null )
        {
            p.setExecute( true );
        }
        if ( map.get( username + ".delete" ) != null )
        {
            p.setDelete( true );
        }
        if ( map.get( username + ".write" ) != null )
        {
            p.setWrite( true );
        }
        if ( map.get( username + ".read" ) != null )
        {
            p.setRead( true );
        }
        if ( map.get( username + ".administer" ) != null )
        {
            p.setAdminister( true );
        }
        return p;
    }
}
