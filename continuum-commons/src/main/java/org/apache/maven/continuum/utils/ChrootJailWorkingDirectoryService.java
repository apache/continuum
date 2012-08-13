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
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import javax.annotation.Resource;

/**
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
@Service( "workingDirectoryService#chrootJail" )
public class ChrootJailWorkingDirectoryService
    implements WorkingDirectoryService
{
    @Resource
    private ConfigurationService configurationService;

    /**
     * @plexus.configuration
     */
    private File chrootJailDirectory;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setChrootJailDirectory( File chrootJailDirectory )
    {
        this.chrootJailDirectory = chrootJailDirectory;
    }

    public File getChrootJailDirectory()
    {
        return chrootJailDirectory;
    }

    public File getWorkingDirectory( Project project )
    {
        return getWorkingDirectory( project, true );
    }

    public File getWorkingDirectory( Project project, boolean shouldSet )
    {
        ProjectGroup projectGroup = project.getProjectGroup();

        File f = new File( getChrootJailDirectory(), projectGroup.getGroupId() );
        f = new File( f, getConfigurationService().getWorkingDirectory().getPath() );
        return new File( f, Integer.toString( project.getId() ) );
    }

    public File getWorkingDirectory( Project project, String projectScmRoot, List<Project> projects )
    {
        return getWorkingDirectory( project, true );
    }

    public File getWorkingDirectory( Project project, String projectScmRoot, List<Project> projects, boolean shouldSet )
    {
        return getWorkingDirectory( project, shouldSet );
    }
}
