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

import org.apache.commons.io.IOUtils;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.builder.utils.ContinuumBuildConstant;
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.utils.file.FileSystemManager;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "buildResult", instantiationStrategy = "per-lookup" )
public class BuildResultAction
    extends AbstractBuildAction
{
    private static Logger log = LoggerFactory.getLogger( BuildResultAction.class );

    @Requirement
    private FileSystemManager fsManager;

    @Requirement
    private DistributedBuildManager distributedBuildManager;

    private Project project;

    private BuildResult buildResult;

    private int buildId;

    private List<ChangeSet> changeSet;

    private boolean hasSurefireResults;

    private String buildOutput;

    private String state;

    private String projectGroupName = "";

    private int projectGroupId;

    public String execute()
        throws ContinuumException, IOException, BuildManagerException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        //todo get this working for other types of test case rendering other then just surefire
        // check if there are surefire results to display
        project = getContinuum().getProject( getProjectId() );

        ConfigurationService configuration = getContinuum().getConfiguration();

        buildResult = getContinuum().getBuildResult( getBuildId() );

        boolean runningOnAgent = false;

        if ( configuration.isDistributedBuildEnabled() )
        {
            try
            {
                int buildDefinitionId = buildResult.getBuildDefinition().getId();
                runningOnAgent = buildResult.getState() == ContinuumProjectState.BUILDING &&
                    distributedBuildManager.getCurrentRun( getProjectId(), buildDefinitionId ).getBuildResultId()
                        == getBuildId();
            }
            catch ( ContinuumException e )
            {
                log.debug( "running distributed build not found: {}", e.getMessage() );
            }
        }

        // view build result of the current build from the distributed build agent
        if ( runningOnAgent )
        {
            Map<String, Object> map = distributedBuildManager.getBuildResult( project.getId() );

            if ( map == null )
            {
                projectGroupId = project.getProjectGroup().getId();

                return ERROR;
            }

            if ( map.size() > 0 )
            {
                buildResult = ContinuumBuildConstant.getBuildResult( map, null );

                buildOutput = ContinuumBuildConstant.getBuildOutput( map );

                if ( ServletActionContext.getRequest() != null )
                {
                    state = StateGenerator.generate( buildResult.getState(),
                                                     ServletActionContext.getRequest().getContextPath() );
                }
            }
            changeSet = null;

            hasSurefireResults = false;

            this.setCanDelete( false );
        }
        else
        {
            buildResult = getContinuum().getBuildResult( getBuildId() );

            // directory contains files ?
            File[] testReports = null;
            try
            {
                File surefireReportsDirectory = configuration.getTestReportsDirectory( buildId, getProjectId() );
                testReports = surefireReportsDirectory.listFiles();
            }
            catch ( ConfigurationException ce )
            {
                log.warn( "failed to access test reports", ce );
            }

            hasSurefireResults = testReports != null && testReports.length > 0;
            changeSet = getContinuum().getChangesSinceLastSuccess( getProjectId(), getBuildId() );

            try
            {
                buildOutput = getBuildOutputText();
            }
            catch ( ConfigurationException ce )
            {
                log.warn( "failed to access build output", ce );
            }

            if ( ServletActionContext.getRequest() != null )
            {
                state = StateGenerator.generate( buildResult.getState(),
                                                 ServletActionContext.getRequest().getContextPath() );
            }

            this.setCanDelete( this.canRemoveBuildResult( buildResult ) );
        }

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
        if ( this.isConfirmed() )
        {
            try
            {
                if ( canRemoveBuildResult( getContinuum().getBuildResult( buildId ) ) )
                {
                    getContinuum().removeBuildResult( buildId );
                }
                else
                {
                    addActionError( getText( "buildResult.cannot.delete" ) );
                }
            }
            catch ( ContinuumException e )
            {
                addActionError( getText( "buildResult.delete.error", "Unable to delete build result", new Integer(
                    buildId ).toString() ) );
            }
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }

            AuditLog event = new AuditLog( "Build Result id=" + buildId, AuditLogConstants.REMOVE_BUILD_RESULT );
            event.setCategory( AuditLogConstants.BUILD_RESULT );
            event.setCurrentUser( getPrincipal() );
            event.log();

            return SUCCESS;
        }

        return CONFIRM;
    }

    public String buildLogAsText()
        throws ConfigurationException, IOException
    {
        buildOutput = getBuildOutputText();
        return SUCCESS;
    }

    public InputStream getBuildOutputInputStream()
        throws ConfigurationException, IOException
    {
        return IOUtils.toInputStream( buildOutput );
    }

    private String getBuildOutputText()
        throws ConfigurationException, IOException
    {
        ConfigurationService configuration = getContinuum().getConfiguration();
        File buildOutputFile = configuration.getBuildOutputFile( getBuildId(), getProjectId() );

        if ( buildOutputFile.exists() )
        {
            return fsManager.fileContents( buildOutputFile );
        }
        return null;
    }

    public int getBuildId()
    {
        return buildId;
    }

    public void setBuildId( int buildId )
    {
        this.buildId = buildId;
    }

    public Project getProject()
    {
        return project;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public List<ChangeSet> getChangesSinceLastSuccess()
    {
        return changeSet;
    }

    public boolean isHasSurefireResults()
    {
        return hasSurefireResults;
    }

    public void setHasSurefireResults( boolean hasSurefireResults )
    {
        this.hasSurefireResults = hasSurefireResults;
    }

    public String getBuildOutput()
    {
        return buildOutput;
    }

    public String getState()
    {
        return state;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( getProjectId() ).getName();
        }

        return projectGroupName;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    // for testing
    public void setDistributedBuildManager( DistributedBuildManager distributedBuildManager )
    {
        this.distributedBuildManager = distributedBuildManager;
    }

    public boolean isBuildInProgress()
    {
        int buildState = buildResult.getState();
        return buildState == ContinuumProjectState.BUILDING;
    }

    public boolean isBuildSuccessful()
    {
        return buildResult.getState() == ContinuumProjectState.OK;
    }

    public boolean isShowBuildNumber()
    {
        return buildResult.getBuildNumber() != 0;
    }

    public boolean isShowBuildError()
    {
        return !isBuildSuccessful() && !StringUtils.isEmpty( buildResult.getError() );
    }
}
