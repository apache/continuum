package org.apache.continuum.buildagent.action;

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

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.scm.ContinuumScm;
import org.apache.continuum.scm.ContinuumScmConfiguration;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.codehaus.plexus.action.AbstractAction;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="changelog-agent-project"
 */
public class ChangeLogProjectAction
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    public void execute( Map context )
        throws Exception
    {
        Project project = ContinuumBuildAgentUtil.getProject( context );

        try
        {
            File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() );
            ContinuumScmConfiguration config = createScmConfiguration( project, workingDirectory );
            config.setLatestUpdateDate( ContinuumBuildAgentUtil.getLatestUpdateDate( context ) );
            getLogger().info( "Getting changeLog of project: " + project.getName() );
            ChangeLogScmResult changeLogResult = scm.changeLog( config );

            if ( !changeLogResult.isSuccess() )
            {
                getLogger().warn( "Error getting change log of project " + project.getName() );

                getLogger().warn( "Command Output: " + changeLogResult.getCommandOutput() );

                getLogger().warn( "Provider Message: " + changeLogResult.getProviderMessage() );
            }

            context.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, getLatestUpdateDate( changeLogResult ) );
        }
        catch ( ScmException e )
        {
            context.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, null );

            getLogger().error( e.getMessage(), e );
        }
    }

    private ContinuumScmConfiguration createScmConfiguration( Project project, File workingDirectory )
    {
        ContinuumScmConfiguration config = new ContinuumScmConfiguration();
        config.setUrl( project.getScmUrl() );
        config.setUsername( project.getScmUsername() );
        config.setPassword( project.getScmPassword() );
        config.setUseCredentialsCache( project.isScmUseCache() );
        config.setWorkingDirectory( workingDirectory );
        config.setTag( project.getScmTag() );

        return config;
    }

    private Date getLatestUpdateDate( ChangeLogScmResult changeLogScmResult )
    {
        ChangeLogSet changeLogSet = changeLogScmResult.getChangeLog();

        if ( changeLogSet != null )
        {
            List<ChangeSet> changes = changeLogSet.getChangeSets();

            if ( changes != null && !changes.isEmpty() )
            {
                long date = 0;

                for ( ChangeSet change : changes )
                {
                    if ( date < change.getDate().getTime() )
                    {
                        date = change.getDate().getTime();
                    }
                }

                if ( date != 0 )
                {
                    return new Date( date );
                }
            }
        }

        return null;
    }

    public void setScm( ContinuumScm scm )
    {
        this.scm = scm;
    }

    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }
}
