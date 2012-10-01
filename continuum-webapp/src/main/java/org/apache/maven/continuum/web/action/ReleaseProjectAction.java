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

import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Edwin Punzalan
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="releaseProject"
 */
public class ReleaseProjectAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String projectName;

    private Map<String, String> preparedReleases;

    private String preparedReleaseId;

    private String goal;

    private String scmUrl;

    private Project project;

    private List releaseList;

    private String projectGroupName = "";

    protected static final String REQUIRES_CONFIGURATION = "releaseOutputDir-required";

    public String promptReleaseGoal()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        // check if releaseOutputDirectory is already set
        if ( getContinuum().getConfiguration().getReleaseOutputDirectory() == null )
        {
            return REQUIRES_CONFIGURATION;
        }

        project = getContinuum().getProjectWithAllDetails( projectId );

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

            preparedReleases = releaseManager.getPreparedReleases( project.getGroupId(), project.getArtifactId() );
        }
        else
        {
            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

            this.preparedReleases = releaseManager.getPreparedReleasesForProject( project.getGroupId(),
                                                                                  project.getArtifactId() );
        }

        if ( !preparedReleases.isEmpty() )
        {
            // use last release as default choice
            preparedReleaseId = new ArrayList<String>( preparedReleases.keySet() ).get( preparedReleases.size() - 1 );
        }
        else
        {
            preparedReleaseId = null;
        }

        projectName = project.getName();

        return SUCCESS;
    }

    public String execute()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        if ( "prepare".equals( goal ) )
        {
            return "prepareRelease";
        }
        else if ( "perform".equals( goal ) )
        {
            if ( "".equals( preparedReleaseId ) )
            {
                return "performReleaseFromScm";
            }
            else
            {
                return "performRelease";
            }
        }
        else
        {
            return "prompt";
        }
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getGoal()
    {
        return goal;
    }

    public void setGoal( String goal )
    {
        this.goal = goal;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject( Project project )
    {
        this.project = project;
    }

    public String getScmUrl()
    {
        return scmUrl;
    }

    public void setScmUrl( String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    public List getReleaseList()
    {
        return releaseList;
    }

    public void setReleaseList( List releaseList )
    {
        this.releaseList = releaseList;
    }

    public String getPreparedReleaseId()
    {
        return preparedReleaseId;
    }

    public void setPreparedReleaseId( String preparedReleaseId )
    {
        this.preparedReleaseId = preparedReleaseId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }

    public Map<String, String> getPreparedReleases()
    {
        return preparedReleases;
    }

    public void setPreparedReleases( Map<String, String> preparedReleases )
    {
        this.preparedReleases = preparedReleases;
    }
}
