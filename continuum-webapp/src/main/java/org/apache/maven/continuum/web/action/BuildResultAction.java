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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.builder.utils.ContinuumBuildConstant;
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;


/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildResult"
 */
public class BuildResultAction
    extends AbstractBuildAction
{
    /**
     * @plexus.requirement
     */
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
        throws ContinuumException, ConfigurationException, IOException, BuildManagerException
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

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() &&
            project.getState() == ContinuumProjectState.BUILDING )
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

                state =
                    StateGenerator.generate( buildResult.getState(), ServletActionContext.getRequest().getContextPath() );
            }
            changeSet = null;

            hasSurefireResults = false;

            this.setCanDelete( false );
        }
        else
        {
            buildResult = getContinuum().getBuildResult( getBuildId() );

            // directory contains files ?
            File surefireReportsDirectory =
                getContinuum().getConfiguration().getTestReportsDirectory( buildId, getProjectId() );
            File[] files = surefireReportsDirectory.listFiles();
            hasSurefireResults = files != null && files.length > 0;
            changeSet = getContinuum().getChangesSinceLastSuccess( getProjectId(), getBuildId() );

            buildOutput = getBuildOutputText();

            state =
                StateGenerator.generate( buildResult.getState(), ServletActionContext.getRequest().getContextPath() );

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
            getContinuum().removeBuildResult( buildId );
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
        String outputText = getBuildOutputText();
        return outputText == null ? null : IOUtils.toInputStream( outputText );
    }

    private String getBuildOutputText()
        throws ConfigurationException, IOException
    {
        File buildOutputFile = getContinuum().getConfiguration().getBuildOutputFile( getBuildId(), getProjectId() );

        if ( buildOutputFile.exists() )
        {
            return StringEscapeUtils.escapeHtml( FileUtils.fileRead( buildOutputFile ) );
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
}
