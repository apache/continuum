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

import java.io.IOException;

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.execution.maven.m2.SettingsConfigurationException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jpox.SchemaTool;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 * @todo use this, reintroduce default project group
 * @plexus.component role="org.apache.maven.continuum.initialization.ContinuumInitializer"
 * role-hint="default"
 */
public class DefaultContinuumInitializer
    extends AbstractLogEnabled
    implements ContinuumInitializer
{
    // ----------------------------------------------------------------------
    //  Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

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
        getLogger().info( "Continuum initializer running ..." );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Dumping JPOX/JDO Schema Details ..." );
            try
            {
                SchemaTool.outputDBInfo( null, true );
                SchemaTool.outputSchemaInfo( null, true );
            }
            catch ( Exception e )
            {
                getLogger().debug( "Error while dumping the database schema", e );
            }
        }

        try
        {
            // System Configuration
            SystemConfiguration systemConf = store.getSystemConfiguration();

            if ( systemConf == null )
            {
                systemConf = new SystemConfiguration();

                systemConf = store.addSystemConfiguration( systemConf );
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
        getLogger().info( "Continuum initializer end running ..." );
    }


    private void createDefaultProjectGroup()
        throws ContinuumStoreException, BuildDefinitionServiceException
    {
        ProjectGroup group;
        try
        {
            group = store.getProjectGroupByGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID );
            getLogger().info( "Default Project Group exists" );
        }
        catch ( ContinuumObjectNotFoundException e )
        {

            getLogger().info( "create Default Project Group" );

            group = new ProjectGroup();

            group.setName( "Default Project Group" );

            group.setGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID );

            group.setDescription( "Contains all projects that do not have a group of their own" );

            LocalRepository localRepository = store.getLocalRepositoryByName( "DEFAULT" );
            
            group.setLocalRepository( localRepository );
            
            group.getBuildDefinitions().addAll(
                buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getBuildDefinitions() );

            group = store.addProjectGroup( group );
        }
    }
    
    private void createDefaultLocalRepository()
        throws ContinuumStoreException, ContinuumInitializationException
    {
        LocalRepository repository;
        
        repository = store.getLocalRepositoryByName( "DEFAULT" );
        
        Settings settings = getSettings();
        
        if ( repository == null )
        {
            getLogger().info( "create Default Local Repository" );
            
            repository = new LocalRepository();
            
            repository.setName( "DEFAULT" );
            
            repository.setLocation( settings.getLocalRepository() );
            
            repository = store.addLocalRepository( repository );
            
            createDefaultPurgeConfiguration( repository );
        }
        else if ( !repository.getLocation().equals( settings.getLocalRepository() ) )
        {
            getLogger().info( "updating location of Default Local Repository" );
            
            repository.setLocation( settings.getLocalRepository() );
            
            store.updateLocalRepository( repository );
        }
    }
    
    private void createDefaultPurgeConfiguration( LocalRepository repository )
        throws ContinuumStoreException
    {
        RepositoryPurgeConfiguration repoPurge = new RepositoryPurgeConfiguration();
        
        repoPurge.setRepository( repository );
        
        repoPurge.setDefaultPurge( true );
        
        store.addRepositoryPurgeConfiguration( repoPurge );
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
