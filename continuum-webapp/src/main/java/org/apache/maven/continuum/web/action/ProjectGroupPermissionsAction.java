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

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.ContinuumException;

import org.apache.maven.user.model.UserManager;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * Action to see and edit project group permissions per user.
 *
 * @author <a href="mailto:hisidro@exist.com">Henry Isidro</a>
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

    private List users;

    public String execute()
        throws ContinuumException
    {
        users = userManager.getUsersInstancePermissions();

        projectGroup = getContinuum().getProjectGroup( projectGroupId );
        
        getLogger().info("ProjectGroupName = " + projectGroup.getName() );

        return INPUT;
    }

    public String save()
        throws ContinuumException
    {
       return SUCCESS;
 
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public List getUsers()
    {
        return users;
    }
}
