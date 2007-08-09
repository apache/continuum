package org.apache.maven.continuum.web.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="projects"
 */
public class ProjectsListAction
    extends ContinuumActionSupport
{
    private Collection selectedProjects;

    private String projectGroupName = "";

    private int projectGroupId;

    public String remove()
        throws ContinuumException
    {
        try
        {
            checkModifyProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        if ( selectedProjects != null && !selectedProjects.isEmpty() )
        {
            for ( Iterator i = selectedProjects.iterator(); i.hasNext(); )
            {
                int projectId = Integer.parseInt( (String) i.next() );

                try
                {
                    getLogger().info( "Removing Project with id=" + projectId );

                    getContinuum().removeProject( projectId );
                }
                catch ( ContinuumException e )
                {
                    getLogger().error( "Error removing Project with id=" + projectId );
                    addActionError( "Unable to remove Project with id=" + projectId );
                }
            }
        }

        return SUCCESS;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
        }

        return projectGroupName;
    }

    public Collection getSelectedProjects()
    {
        return selectedProjects;
    }

    public void setSelectedProjects( Collection selectedProjects )
    {
        this.selectedProjects = selectedProjects;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }
}