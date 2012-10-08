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

import org.apache.continuum.configuration.BuildAgentConfigurationException;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Edwin Punzalan
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="releaseCleanup"
 */
public class ReleaseCleanupAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    private String projectGroupName = "";

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

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

            try
            {
                String goal = releaseManager.releaseCleanup( releaseId );

                if ( StringUtils.isNotBlank( goal ) )
                {
                    return goal;
                }
                else
                {
                    throw new Exception( "No listener to cleanup for id " + releaseId );
                }
            }
            catch ( BuildAgentConfigurationException e )
            {
                List<Object> args = new ArrayList<Object>();
                args.add( e.getMessage() );

                addActionError( getText( "releaseCleanup.error", args ) );
                return RELEASE_ERROR;
            }
        }
        else
        {
            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

            releaseManager.getReleaseResults().remove( releaseId );

            ContinuumReleaseManagerListener listener =
                (ContinuumReleaseManagerListener) releaseManager.getListeners().remove( releaseId );

            if ( listener != null )
            {
                String goal = listener.getGoalName();

                return goal + "Finished";
            }
            else
            {
                throw new Exception( "No listener to cleanup for id " + releaseId );
            }
        }
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( projectGroupName == null || "".equals( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }

    public int getProjectGroupId()
        throws ContinuumException
    {
        return getContinuum().getProjectGroupByProjectId( projectId ).getId();
    }
}
