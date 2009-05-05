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
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

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
    
    private static final Logger log = LoggerFactory.getLogger( DefaultWorkingDirectoryService.class );

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
        return getWorkingDirectory( project, null, null );
    }
    
    /**
     * 
     * @param project
     * @param projectScmRoot
     * @param projects projects under the same projectScmRoot
     * @return
     */
    public File getWorkingDirectory( Project project, String projectScmRoot, List<Project> projects )
    {
//        TODO: Enable, this is what we really want
//        ContinuumProjectGroup projectGroup = project.getProjectGroup();
//
//        return new File( projectGroup.getWorkingDirectory(),
//                         project.getPath() );
        
        if ( project.getWorkingDirectory() == null || "".equals( project.getWorkingDirectory() ) )
        {   
            if ( project.isCheckedOutInSingleDirectory() && projectScmRoot != null && !"".equals( projectScmRoot ) )
            {
                Project rootProject = project;
                if( projects != null )
                {
                    // the root project should have the lowest id since it's always added first                    
                    for( Project projectUnderScmRoot : projects )
                    {
                        if( projectUnderScmRoot.getId() < rootProject.getId() )
                        {
                            rootProject = projectUnderScmRoot;
                        }
                    }
                }                
                
             // determine the path
                String projectScmUrl = project.getScmUrl();
                int indexDiff = StringUtils.differenceAt( projectScmUrl, projectScmRoot );
                
                String pathToProject = projectScmUrl.substring( indexDiff );      
                if( pathToProject.startsWith( "\\" ) || pathToProject.startsWith( "/" ) )
                {
                    project.setWorkingDirectory( Integer.toString( rootProject.getId() ) + pathToProject );
                }
                else
                {
                    project.setWorkingDirectory( Integer.toString( rootProject.getId() ) + "/" + pathToProject );
                }                
            }
            else
            {
                project.setWorkingDirectory( Integer.toString( project.getId() ) );
            }
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
            File baseWorkingDir = getConfigurationService().getWorkingDirectory();            
            
            // windows path
            if( baseWorkingDir.getPath().indexOf( '\\' ) != -1 )
            {
                project.setWorkingDirectory( project.getWorkingDirectory().replace( '/', '\\' ) );
                workDir = new File( baseWorkingDir.getPath() + "\\" + project.getWorkingDirectory() );
            }
            else
            {
                workDir = new File( baseWorkingDir, project.getWorkingDirectory() );
            }            
        }
        
        return workDir;
    }
}
