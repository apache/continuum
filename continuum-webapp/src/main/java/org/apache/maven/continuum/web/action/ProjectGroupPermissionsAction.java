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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.user.model.InstancePermissions;
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

    private Map map = new HashMap();

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

    public String execute()
        throws ContinuumException
    {
        List userPermissionsAsList = userManager.getUsersInstancePermissions( ProjectGroup.class,
                                                                              new Integer( projectGroupId ) );

        setUserPermissions( (InstancePermissions[]) userPermissionsAsList.toArray( new InstancePermissions[0] ) );

//        getUserPermissions()[0].setBuild( true );
//        getUserPermissions()[1].setDelete( true );
        
        setProjectGroup( getContinuum().getProjectGroup( projectGroupId ) );

        getLogger().info( "ProjectGroupName = " + getProjectGroup().getName() );

        return INPUT;
    }

    public String save()
        throws ContinuumException
    {
        for ( Iterator i = map.keySet().iterator(); i.hasNext(); )
        {
            String id = (String) i.next();
            getLogger().info( "key value == " + id );
        }
        for ( Iterator i = map.values().iterator(); i.hasNext(); )
        {
            String[] id = (String[]) i.next();
            for ( int y = 0; y < id.length; y++ )
            {
                getLogger().info( "class name == " + id[y].getClass().getSimpleName() );
                getLogger().info( "value == " + id[y] );
            }
        }
        // TODO - compare the values from the map to userManager.getUsersInstancePermissions() then save the differences
        // NOTE: only the checkboxes that are checked are saved on the map.
        //       each user has 4 possible checkboxes and all of them are saved into an array of String.
        //       so to determine each checkbox(view) from the others(edit/delete/build), we need a special notation. see below...
        // KEY FORMAT: username + view/edit/delete or build. sample would be adminbuild or guestview
        return SUCCESS;
    }
}
