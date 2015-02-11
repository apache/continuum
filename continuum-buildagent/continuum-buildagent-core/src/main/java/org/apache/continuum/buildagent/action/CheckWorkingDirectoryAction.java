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
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.util.Map;


@Component( role = org.codehaus.plexus.action.Action.class, hint = "check-agent-working-directory" )
public class CheckWorkingDirectoryAction
    extends AbstractAction
{

    @Requirement
    BuildAgentConfigurationService buildAgentConfigurationService;

    public void execute( Map context )
        throws Exception
    {
        Project project = ContinuumBuildAgentUtil.getProject( context );

        File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( project.getId() );

        if ( !workingDirectory.exists() )
        {
            context.put( ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY_EXISTS, Boolean.FALSE );

            return;
        }

        File[] files = workingDirectory.listFiles();

        context.put( ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY_EXISTS, files.length > 0 );
    }

}
