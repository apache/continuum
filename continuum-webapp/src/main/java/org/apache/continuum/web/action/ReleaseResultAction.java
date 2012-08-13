package org.apache.continuum.web.action;

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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.shared.release.ReleaseResult;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="releaseResult"
 */
public class ReleaseResultAction
    extends ContinuumConfirmAction
{
    private static final Logger logger = LoggerFactory.getLogger( ReleaseResultAction.class );

    private int projectGroupId;

    private int releaseResultId;

    private List<ContinuumReleaseResult> releaseResults;

    private List<String> selectedReleaseResults;

    private ProjectGroup projectGroup;

    private ReleaseResult result;

    private boolean confirmed;

    private String projectName;

    private String releaseGoal;

    private String username;

    public String list()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        releaseResults = getContinuum().getContinuumReleaseResultsByProjectGroup( projectGroupId );

        return SUCCESS;

    }

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

        if ( confirmed )
        {
            if ( selectedReleaseResults != null && !selectedReleaseResults.isEmpty() )
            {
                for ( String id : selectedReleaseResults )
                {
                    int resultId = Integer.parseInt( id );

                    try
                    {
                        logger.info( "Removing ContinuumReleaseResult with id=" + resultId );

                        getContinuum().removeContinuumReleaseResult( resultId );
                    }
                    catch ( ContinuumException e )
                    {
                        logger.error( "Error removing ContinuumReleaseResult with id=" + resultId );
                        addActionError( getText( "Unable to remove ContinuumReleaseResult with id=" + resultId ) );
                    }
                }
            }
            return SUCCESS;
        }

        return CONFIRM;
    }

    public String viewResult()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        ContinuumReleaseResult releaseResult = getContinuum().getContinuumReleaseResult( releaseResultId );

        result = new ReleaseResult();
        result.setStartTime( releaseResult.getStartTime() );
        result.setEndTime( releaseResult.getEndTime() );
        result.setResultCode( releaseResult.getResultCode() );

        releaseGoal = releaseResult.getReleaseGoal();
        projectName = releaseResult.getProject().getName();
        username = releaseResult.getUsername();

        try
        {
            File releaseOutputFile = getContinuum().getConfiguration().getReleaseOutputFile( projectGroupId,
                                                                                             "releases-" +
                                                                                                 releaseResult.getStartTime() );

            if ( releaseOutputFile.exists() )
            {
                String str = StringEscapeUtils.escapeHtml( FileUtils.fileRead( releaseOutputFile ) );
                result.appendOutput( str );
            }
        }
        catch ( ConfigurationException e )
        {
            //getLogger().error( "" );
        }
        catch ( IOException e )
        {
            //getLogger().error( "" );
        }

        return SUCCESS;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {

        return getProjectGroup( projectGroupId ).getName();
    }

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        if ( projectGroup == null )
        {
            projectGroup = getContinuum().getProjectGroup( projectGroupId );
        }
        else
        {
            if ( projectGroup.getId() != projectGroupId )
            {
                projectGroup = getContinuum().getProjectGroup( projectGroupId );
            }
        }

        return projectGroup;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getReleaseResultId()
    {
        return releaseResultId;
    }

    public void setReleaseResultId( int releaseResultId )
    {
        this.releaseResultId = releaseResultId;
    }

    public List<ContinuumReleaseResult> getReleaseResults()
    {
        return releaseResults;
    }

    public void setReleaseResults( List<ContinuumReleaseResult> releaseResults )
    {
        this.releaseResults = releaseResults;
    }

    public List<String> getSelectedReleaseResults()
    {
        return selectedReleaseResults;
    }

    public void setSelectedReleaseResults( List<String> selectedReleaseResults )
    {
        this.selectedReleaseResults = selectedReleaseResults;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public String getReleaseGoal()
    {
        return releaseGoal;
    }

    public void setReleaseGoal( String releaseGoal )
    {
        this.releaseGoal = releaseGoal;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }
}
