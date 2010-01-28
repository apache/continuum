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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.configuration.BuildAgentConfigurationException;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.ReleaseListenerSummary;
import org.apache.maven.shared.release.ReleaseResult;

/**
 * @author Edwin Punzalan
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="releaseInProgress"
 */
public class ReleaseInProgressAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    private String releaseGoal;

    private ContinuumReleaseManagerListener listener;

    private ReleaseResult result;

    private String projectGroupName = "";

    private ReleaseListenerSummary listenerSummary;

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

        String status = "";

        listenerSummary = new ReleaseListenerSummary();

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

            Map map; 

            try
            {
                map = releaseManager.getListener( releaseId );
            }
            catch ( BuildAgentConfigurationException e )
            {
                List<String> args = new ArrayList<String>();
                args.add( e.getMessage() );

                addActionError( getText( "distributedBuild.releaseInProgress.error", args ) );
                return ERROR;
            }

            if ( map != null && !map.isEmpty() )
            {
                int state = DistributedReleaseUtil.getReleaseState( map );

                if ( state == ContinuumReleaseManagerListener.LISTENING )
                {
                    status = "inProgress";
                }
                else if ( state == ContinuumReleaseManagerListener.FINISHED )
                {
                    status = SUCCESS;
                }
                else
                {
                    status = "initialized";
                }
    
                if ( status.equals( SUCCESS ) )
                {
                    ReleaseResult result = releaseManager.getReleaseResult( releaseId );
    
                    if ( result != null && getContinuum().getContinuumReleaseResult( projectId, releaseGoal, result.getStartTime(), result.getEndTime() ) == null )
                    {
                        ContinuumReleaseResult releaseResult = createContinuumReleaseResult( result );
                        getContinuum().addContinuumReleaseResult( releaseResult );
                    }
                }

                listenerSummary.setPhases( DistributedReleaseUtil.getReleasePhases( map ) );
                listenerSummary.setCompletedPhases( DistributedReleaseUtil.getCompletedReleasePhases( map ) );
                listenerSummary.setInProgress( DistributedReleaseUtil.getReleaseInProgress( map ) );
                listenerSummary.setError( DistributedReleaseUtil.getReleaseError( map ) );
            }
            else
            {
                throw new Exception( "There is no on-going or finished release operation with id " + releaseId );
            }
        }
        else
        {
            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();
    
            listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get( releaseId );
    
            if ( listener != null )
            {
                if ( listener.getState() == ContinuumReleaseManagerListener.LISTENING )
                {
                    status = "inProgress";
                }
                else if ( listener.getState() == ContinuumReleaseManagerListener.FINISHED )
                {
                    status = SUCCESS;
                }
                else
                {
                    status = "initialized";
                }

                listenerSummary.setPhases( listener.getPhases() );
                listenerSummary.setCompletedPhases( listener.getCompletedPhases() );
                listenerSummary.setInProgress( listener.getInProgress() );
                listenerSummary.setError( listener.getError() );
            }
            else
            {
                throw new Exception( "There is no on-going or finished release operation with id " + releaseId );
            }

            if ( status.equals( SUCCESS ) )
            {
                ReleaseResult result = (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );

                if ( result != null && getContinuum().getContinuumReleaseResult( projectId, releaseGoal, result.getStartTime(), result.getEndTime() ) == null )
                {
                    ContinuumReleaseResult releaseResult = createContinuumReleaseResult( result );
                    getContinuum().addContinuumReleaseResult( releaseResult );
                }
            }
        }

        return status;
    }

    public String viewResult()
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

        listenerSummary = new ReleaseListenerSummary();

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

            try
            {
                Map map = releaseManager.getListener( releaseId );
    
                if ( map != null && !map.isEmpty() )
                {
                    int state = DistributedReleaseUtil.getReleaseState( map );
    
                    listenerSummary.setPhases( DistributedReleaseUtil.getReleasePhases( map ) );
                    listenerSummary.setCompletedPhases( DistributedReleaseUtil.getCompletedReleasePhases( map ) );
                    listenerSummary.setInProgress( DistributedReleaseUtil.getReleaseInProgress( map ) );
                    listenerSummary.setError( DistributedReleaseUtil.getReleaseError( map ) );
    
                    if ( state == ContinuumReleaseManagerListener.FINISHED )
                    {
                        result = releaseManager.getReleaseResult( releaseId );
        
                        return SUCCESS;
                    }
                    else
                    {
                        throw new Exception( "The release operation with id " + releaseId + "has not finished yet." );
                    }
                }
                else
                {
                    throw new Exception( "There is no finished release operation with id " + releaseId );
                }
            }
            catch ( BuildAgentConfigurationException e )
            {
                List<String> args = new ArrayList<String>();
                args.add( e.getMessage() );
                
                addActionError( getText( "releaseViewResult.error", args ) );
                return ERROR;
            }
        }
        else
        {
            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();
    
            listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get( releaseId );
    
            if ( listener != null )
            {
                listenerSummary.setPhases( listener.getPhases() );
                listenerSummary.setCompletedPhases( listener.getCompletedPhases() );
                listenerSummary.setInProgress( listener.getInProgress() );
                listenerSummary.setError( listener.getError() );

                if ( listener.getState() == ContinuumReleaseManagerListener.FINISHED )
                {
                    result = (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );
    
                    return SUCCESS;
                }
                else
                {
                    throw new Exception( "The release operation with id " + releaseId + "has not finished yet." );
                }
            }
            else
            {
                throw new Exception( "There is no finished release operation with id " + releaseId );
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

    public ContinuumReleaseManagerListener getListener()
    {
        return listener;
    }

    public void setListener( ContinuumReleaseManagerListener listener )
    {
        this.listener = listener;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getReleaseGoal()
    {
        return releaseGoal;
    }

    public void setReleaseGoal( String releaseGoal )
    {
        this.releaseGoal = releaseGoal;
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

    public ReleaseListenerSummary getListenerSummary()
    {
        return listenerSummary;
    }

    public void setListenerSummary( ReleaseListenerSummary listenerSummary )
    {
        this.listenerSummary = listenerSummary;
    }

    private ContinuumReleaseResult createContinuumReleaseResult( ReleaseResult result )
        throws ContinuumException
    {
        ContinuumReleaseResult releaseResult = new ContinuumReleaseResult();
        releaseResult.setStartTime( result.getStartTime() );
        releaseResult.setEndTime( result.getEndTime() );
        releaseResult.setResultCode( result.getResultCode() );

        Project project = getContinuum().getProject( projectId );
        ProjectGroup projectGroup = project.getProjectGroup();
        releaseResult.setProjectGroup( projectGroup );
        releaseResult.setProject( project );
        releaseResult.setReleaseGoal( releaseGoal );

        String releaseName = "releases-" + result.getStartTime();

        try
        {
            File logFile = getContinuum().getConfiguration().getReleaseOutputFile( projectGroup.getId(), releaseName );

            PrintWriter writer = new PrintWriter( new FileWriter( logFile ) );
            writer.write( result.getOutput() );
            writer.close();
        }
        catch ( ConfigurationException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new ContinuumException( "Unable to write output to file", e );
        }

        return releaseResult;
    }
    
    public String getProjectName()
        throws ContinuumException
    {
        return getProjectGroupName();
    }

}
