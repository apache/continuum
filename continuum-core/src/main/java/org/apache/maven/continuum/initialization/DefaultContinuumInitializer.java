package org.apache.maven.continuum.initialization;

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

import org.apache.continuum.dao.LocalRepositoryDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.dao.SystemConfigurationDao;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jpox.SchemaTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 * @todo use this, reintroduce default project group
 * @plexus.component role="org.apache.maven.continuum.initialization.ContinuumInitializer"
 * role-hint="default"
 */
public class DefaultContinuumInitializer
    implements ContinuumInitializer
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuumInitializer.class );

    // ----------------------------------------------------------------------
    //  Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private LocalRepositoryDao localRepositoryDao;

    /**
     * @plexus.requirement
     */
    private RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    /**
     * @plexus.requirement
     */
    private ProjectGroupDao projectGroupDao;

    /**
     * @plexus.requirement
     */
    private SystemConfigurationDao systemConfigurationDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionService buildDefinitionService;

    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder mavenSettingsBuilder;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize()
        throws ContinuumInitializationException
    {
        log.info( "Continuum initializer running ..." );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Dumping JPOX/JDO Schema Details ..." );
            try
            {
                SchemaTool.outputDBInfo( null, true );
                SchemaTool.outputSchemaInfo( null, true );
            }
            catch ( Exception e )
            {
                log.debug( "Error while dumping the database schema", e );
            }
        }

        try
        {
            // System Configuration
            SystemConfiguration systemConf = systemConfigurationDao.getSystemConfiguration();

            if ( systemConf == null )
            {
                systemConf = new SystemConfiguration();

                systemConf = systemConfigurationDao.addSystemConfiguration( systemConf );
            }

            createDefaultLocalRepository();

            createDefaultProjectGroup();
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumInitializationException( "Can't initialize default schedule.", e );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumInitializationException( "Can't get default build definition", e );
        }
        log.info( "Continuum initializer end running ..." );
    }


    private void createDefaultProjectGroup()
        throws ContinuumStoreException, BuildDefinitionServiceException
    {
        ProjectGroup group;
        try
        {
            group = projectGroupDao.getProjectGroupByGroupId( DEFAULT_PROJECT_GROUP_GROUP_ID );
            log.info( "Default Project Group exists" );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            Collection<ProjectGroup> pgs = projectGroupDao.getAllProjectGroups();
            if ( pgs != null && pgs.isEmpty() )
            {
                log.info( "create Default Project Group" );

                group = new ProjectGroup();

                group.setName( "Default Project Group" );

                group.setGroupId( DEFAULT_PROJECT_GROUP_GROUP_ID );

                group.setDescription( "Contains all projects that do not have a group of their own" );

                LocalRepository localRepository = localRepositoryDao.getLocalRepositoryByName( "DEFAULT" );

                group.setLocalRepository( localRepository );

                group = projectGroupDao.addProjectGroup( group );

                BuildDefinitionTemplate bdt = buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate();

                buildDefinitionService.addBuildDefinitionTemplateToProjectGroup( group.getId(), bdt );
            }
        }
    }

    private void createDefaultLocalRepository()
        throws ContinuumStoreException, ContinuumInitializationException
    {
        LocalRepository repository;

        repository = localRepositoryDao.getLocalRepositoryByName( "DEFAULT" );

        Settings settings = getSettings();

        if ( repository == null )
        {
            log.info( "create Default Local Repository" );

            repository = new LocalRepository();

            repository.setName( "DEFAULT" );

            repository.setLocation( settings.getLocalRepository() );

            repository = localRepositoryDao.addLocalRepository( repository );

            createDefaultPurgeConfiguration( repository );
        }
        else if ( !repository.getLocation().equals( settings.getLocalRepository() ) )
        {
            log.info( "updating location of Default Local Repository" );

            repository.setLocation( settings.getLocalRepository() );

            localRepositoryDao.updateLocalRepository( repository );
        }
    }

    private void createDefaultPurgeConfiguration( LocalRepository repository )
        throws ContinuumStoreException
    {
        RepositoryPurgeConfiguration repoPurge = new RepositoryPurgeConfiguration();

        repoPurge.setRepository( repository );

        repoPurge.setDefaultPurge( true );

        repositoryPurgeConfigurationDao.addRepositoryPurgeConfiguration( repoPurge );
    }

    private Settings getSettings()
        throws ContinuumInitializationException
    {
        try
        {
            return mavenSettingsBuilder.buildSettings( false );
        }
        catch ( IOException e )
        {
            throw new ContinuumInitializationException( "Error reading settings file", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ContinuumInitializationException( e.getMessage(), e );
        }
    }
}
