package org.apache.maven.continuum.utils;

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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Project;
import org.springframework.stereotype.Service;

import java.io.File;

import javax.annotation.Resource;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Service("workingDirectoryService")
public class DefaultWorkingDirectoryService
    implements WorkingDirectoryService
{
    @Resource
    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    // ----------------------------------------------------------------------
    // WorkingDirectoryService Implementation
    // ----------------------------------------------------------------------

    public File getWorkingDirectory( Project project )
    {
//        TODO: Enable, this is what we really want
//        ContinuumProjectGroup projectGroup = project.getProjectGroup();
//
//        return new File( projectGroup.getWorkingDirectory(),
//                         project.getPath() );

        if ( project.getWorkingDirectory() == null )
        {
            project.setWorkingDirectory( Integer.toString( project.getId() ) );
        }

        File workDir;
        File projectWorkingDirectory = new File( project.getWorkingDirectory() );
        if ( projectWorkingDirectory.isAbsolute() )
        {
            // clean the project working directory path if it's a subdirectory of the global working directory
            if ( projectWorkingDirectory.getAbsolutePath().startsWith(
                getConfigurationService().getWorkingDirectory().getAbsolutePath() ) )
            {
                String pwd = projectWorkingDirectory.getAbsolutePath().substring(
                    getConfigurationService().getWorkingDirectory().getAbsolutePath().length() );
                if ( pwd.startsWith( "/" ) || pwd.startsWith( "\\" ) )
                {
                    pwd = pwd.substring( 1 );
                }
                project.setWorkingDirectory( pwd );
            }

            workDir = projectWorkingDirectory;
        }
        else
        {
            workDir = new File( getConfigurationService().getWorkingDirectory(), project.getWorkingDirectory() );
        }
        return workDir;
    }
}
