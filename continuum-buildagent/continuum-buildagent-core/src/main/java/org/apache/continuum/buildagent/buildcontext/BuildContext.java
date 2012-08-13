package org.apache.continuum.buildagent.buildcontext;

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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.scm.ScmResult;

import java.util.Date;
import java.util.Map;

public class BuildContext
{
    private int projectId;

    private String projectName;

    private String projectVersion;

    private int projectState;

    private int buildNumber;

    private int buildDefinitionId;

    private String buildDefinitionLabel;

    private String buildFile;

    private String goals;

    private String arguments;

    private String executorId;

    private String scmUrl;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private int trigger;

    private String username;

    private boolean buildFresh;

    private int projectGroupId;

    private String projectGroupName;

    private String scmRootAddress;

    private int scmRootId;

    private Map<String, Object> actionContext;

    private ScmResult scmResult;

    private BuildResult buildResult;

    private long buildStartTime;

    private String localRepository;

    private ScmResult oldScmResult;

    private Date latestUpdateDate;

    private String buildAgentUrl;

    private int maxExecutionTime;

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getScmRootAddress()
    {
        return scmRootAddress;
    }

    public void setScmRootAddress( String scmRootAddress )
    {
        this.scmRootAddress = scmRootAddress;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public int getProjectState()
    {
        return projectState;
    }

    public void setProjectState( int projectState )
    {
        this.projectState = projectState;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile( String buildFile )
    {
        this.buildFile = buildFile;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments( String arguments )
    {
        this.arguments = arguments;
    }

    public String getExecutorId()
    {
        return executorId;
    }

    public void setExecutorId( String executorId )
    {
        this.executorId = executorId;
    }

    public String getScmUrl()
    {
        return scmUrl;
    }

    public void setScmUrl( String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public String getScmTag()
    {
        return scmTag;
    }

    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public void setTrigger( int trigger )
    {
        this.trigger = trigger;
    }

    public boolean isBuildFresh()
    {
        return buildFresh;
    }

    public void setBuildFresh( boolean buildFresh )
    {
        this.buildFresh = buildFresh;
    }

    public Map<String, Object> getActionContext()
    {
        return actionContext;
    }

    public void setActionContext( Map<String, Object> actionContext )
    {
        this.actionContext = actionContext;
    }

    public ScmResult getScmResult()
    {
        return scmResult;
    }

    public void setScmResult( ScmResult scmResult )
    {
        this.scmResult = scmResult;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public void setBuildResult( BuildResult buildResult )
    {
        this.buildResult = buildResult;
    }

    public long getBuildStartTime()
    {
        return buildStartTime;
    }

    public void setBuildStartTime( long buildStartTime )
    {
        this.buildStartTime = buildStartTime;
    }

    public String getLocalRepository()
    {
        return localRepository;
    }

    public void setLocalRepository( String localRepository )
    {
        this.localRepository = localRepository;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectVersion( String projectVersion )
    {
        this.projectVersion = projectVersion;
    }

    public String getProjectVersion()
    {
        return projectVersion;
    }

    public void setBuildNumber( int buildNumber )
    {
        this.buildNumber = buildNumber;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setOldScmResult( ScmResult oldScmResult )
    {
        this.oldScmResult = oldScmResult;
    }

    public ScmResult getOldScmResult()
    {
        return oldScmResult;
    }

    public void setLatestUpdateDate( Date latestUpdateDate )
    {
        this.latestUpdateDate = latestUpdateDate;
    }

    public Date getLatestUpdateDate()
    {
        return latestUpdateDate;
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public void setMaxExecutionTime( int maxExecutionTime )
    {
        this.maxExecutionTime = maxExecutionTime;
    }

    public int getMaxExecutionTime()
    {
        return maxExecutionTime;
    }

    public void setScmRootId( int scmRootId )
    {
        this.scmRootId = scmRootId;
    }

    public int getScmRootId()
    {
        return scmRootId;
    }

    public void setBuildDefinitionLabel( String buildDefinitionLabel )
    {
        this.buildDefinitionLabel = buildDefinitionLabel;
    }

    public String getBuildDefinitionLabel()
    {
        return buildDefinitionLabel;
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
