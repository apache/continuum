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

import java.io.File;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="clean-agent-working-directory"
 */
public class CleanWorkingDirectoryAction
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    public void execute( Map context )
        throws Exception
    {
        Project project = ContinuumBuildAgentUtil.getProject( context );
    
        File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() );
    
        if ( workingDirectory.exists() )
        {
            getLogger().debug( "Cleaning working directory " + workingDirectory.getAbsolutePath() );

            FileSetManager fileSetManager = new FileSetManager();
            FileSet fileSet = new FileSet();
            fileSet.setDirectory( workingDirectory.getPath() );
            fileSet.addInclude( "**/**" );
            // TODO : this with a configuration option somewhere ?
            fileSet.setFollowSymlinks( false );
            fileSetManager.delete( fileSet );
        }
    }
}
