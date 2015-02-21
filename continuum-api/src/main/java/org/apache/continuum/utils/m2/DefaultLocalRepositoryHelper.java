package org.apache.continuum.utils.m2;

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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.continuum.execution.SettingsConfigurationException;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

@Component( role = LocalRepositoryHelper.class, hint = "default" )
public class DefaultLocalRepositoryHelper
    implements LocalRepositoryHelper
{
    @Requirement
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    @Requirement
    private ArtifactRepositoryLayout repositoryLayout;

    @Requirement
    private SettingsHelper settingsHelper;

    @Configuration( "${plexus.home}/local-repository" )
    private String localRepository;

    public ArtifactRepository getLocalRepository()
        throws SettingsConfigurationException
    {
        return getRepository( null, settingsHelper.getSettings() );
    }

    public ArtifactRepository getLocalRepository( LocalRepository localRepo )
        throws SettingsConfigurationException
    {
        return getRepository( localRepo, settingsHelper.getSettings() );
    }

    private ArtifactRepository getRepository( LocalRepository repository, Settings settings )
    {
        // ----------------------------------------------------------------------
        // Set our configured location as the default but try to use the defaults
        // as returned by the MavenSettings component.
        // ----------------------------------------------------------------------

        String localRepo = localRepository;

        if ( repository != null )
        {
            return artifactRepositoryFactory.createArtifactRepository( repository.getName(),
                                                                       "file://" + repository.getLocation(),
                                                                       repositoryLayout, null, null );
        }
        else if ( !( StringUtils.isEmpty( settings.getLocalRepository() ) ) )
        {
            localRepo = settings.getLocalRepository();
        }

        return artifactRepositoryFactory.createArtifactRepository( "local", "file://" + localRepo, repositoryLayout,
                                                                   null, null );
    }

    public LocalRepository convertAgentRepo( org.apache.continuum.buildagent.model.LocalRepository repo )
    {
        LocalRepository result = new LocalRepository();
        result.setLayout( repo.getLayout() );
        result.setName( repo.getName() );
        result.setLocation( repo.getLocation() );
        return result;
    }
}
