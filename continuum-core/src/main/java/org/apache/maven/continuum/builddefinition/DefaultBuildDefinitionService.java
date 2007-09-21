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
package org.apache.maven.continuum.builddefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 15 sept. 07
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.builddefinition.BuildDefinitionService"
 * @TODO some cache mechanism ?
 */
public class DefaultBuildDefinitionService
    extends AbstractLogEnabled
    implements BuildDefinitionService, Initializable
{

    /**
     * @plexus.configuration default-value=""
     */
    private String defaultAntGoals;

    /**
     * @plexus.configuration default-value=""
     */
    private String defaultAntArguments;

    /**
     * @plexus.configuration default-value="clean:clean jar:install"
     */
    private String defaultM1Goals;

    /**
     * @plexus.configuration default-value=""
     */
    private String defaultM1Arguments;

    /**
     * @plexus.configuration default-value="clean install"
     */
    private String defaultM2Goals;

    /**
     * @plexus.configuration default-value="--batch-mode --non-recursive"
     */
    private String defaultM2Arguments;
    
    
    
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;
    
    /**
     * @plexus.requirement role-hint="default"
     */    
    private ConfigurationService configurationService;
    

    // -----------------------------------------------
    //  Plexus Lifecycle
    // -----------------------------------------------

    public void initialize()
        throws InitializationException
    {
        try
        {
            initializeDefaultContinuumBuildDefintions();
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }
    } 
    
    private void initializeDefaultContinuumBuildDefintions()
        throws BuildDefinitionServiceException
    {
        this.getDefaultAntBuildDefinitionTemplate();
        this.getDefaultMavenOneBuildDefinitionTemplate();
        this.getDefaultMavenTwoBuildDefinitionTemplate();
        this.getDefaultShellBuildDefinitionTemplate();
    }        
    
    public BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getBuildDefinition( buildDefinitionId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            return null;
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public BuildDefinition addBuildDefinition( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.addBuildDefinition( buildDefinition );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    
    public void removeBuildDefinition( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException
    {
        try
        {
            store.removeBuildDefinition( buildDefinition );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public void updateBuildDefinition( BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException
    {
        try
        {
            BuildDefinition storedBuildDefinition = store.getBuildDefinition( buildDefinition.getId() );
            storedBuildDefinition.setBuildFresh( buildDefinition.isBuildFresh() );
            storedBuildDefinition.setAlwaysBuild( buildDefinition.isAlwaysBuild() );
            storedBuildDefinition.setArguments( buildDefinition.getArguments() );
            storedBuildDefinition.setBuildFile( buildDefinition.getBuildFile() );
            storedBuildDefinition.setDefaultForProject( buildDefinition.isDefaultForProject() );
            storedBuildDefinition.setDescription( buildDefinition.getDescription() );
            storedBuildDefinition.setGoals( buildDefinition.getGoals() );
            storedBuildDefinition.setProfile( buildDefinition.getProfile() );
            storedBuildDefinition.setSchedule( buildDefinition.getSchedule() );
            storedBuildDefinition.setType( buildDefinition.getType() );
            store.storeBuildDefinition( storedBuildDefinition );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }

    }

    public List<BuildDefinition> getAllBuildDefinitions()
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getAllBuildDefinitions();
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }
    
    
    public List<BuildDefinition> getAllTemplates()
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getAllTemplates();
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }        
    }

    /** 
     * @see org.apache.maven.continuum.builddefinition.BuildDefinitionService#cloneBuildDefinition(org.apache.maven.continuum.model.project.BuildDefinition)
     */
    public BuildDefinition cloneBuildDefinition( BuildDefinition buildDefinition )
    {
        BuildDefinition cloned = new BuildDefinition();
        cloned.setAlwaysBuild( buildDefinition.isAlwaysBuild() );
        cloned.setArguments( buildDefinition.getArguments() );
        cloned.setBuildFile( buildDefinition.getBuildFile() );
        cloned.setBuildFresh( buildDefinition.isBuildFresh() );
        cloned.setDefaultForProject( buildDefinition.isDefaultForProject() );
        cloned.setDescription( buildDefinition.getDescription() );
        cloned.setGoals( buildDefinition.getGoals() );
        cloned.setProfile( buildDefinition.getProfile() );
        cloned.setSchedule( buildDefinition.getSchedule() );
        cloned.setType( buildDefinition.getType() );
        cloned.setTemplate( buildDefinition.isTemplate() );
        return cloned;
    }
    
    

    public BuildDefinitionTemplate getContinuumDefaultWithType( String type )
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getContinuumBuildDefinitionTemplateWithType( type );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public BuildDefinitionTemplate getDefaultAntBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        if ( template != null )
        {
            return template;
        }
        getLogger().info( "create default AntBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "default ant template" );
        template.setType( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR  );

        template = addBuildDefinitionTemplate( template );

        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( defaultAntGoals );

        bd.setArguments( defaultAntArguments );

        bd.setBuildFile( "build.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setDescription( "default ant buildDefinition" );
        
        bd.setTemplate( true );
        
        bd.setType( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        return addBuildDefinitionInTemplate( template, bd, true );
    }

    public BuildDefinitionTemplate getDefaultMavenOneBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );
        if ( template != null )
        {
            getLogger().debug( "found default maven template " + template.getType() );
            return template;
        }
        getLogger().info( "create default MavenOneBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "default maven1 template" );
        template.setType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR  );
        
        template = addBuildDefinitionTemplate( template );
        
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setArguments( defaultM1Arguments );

        bd.setGoals( defaultM1Goals );

        bd.setBuildFile( "project.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );

        bd.setDescription( "default maven1 buildDefinition" );
        
        bd.setTemplate( true );
        
        return addBuildDefinitionInTemplate( template, bd, true );
    }

    public BuildDefinitionTemplate getDefaultMavenTwoBuildDefinitionTemplate()
        throws  BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        if ( template != null )
        {
            return template;
        }
        getLogger().info( "create default MavenTwoBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "default maven2 template" );
        template.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR  );
        
        template = addBuildDefinitionTemplate( template );        
        
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( this.defaultM2Goals );

        bd.setArguments( this.defaultM2Arguments );

        bd.setBuildFile( "pom.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );

        bd.setDescription( "default maven2 buildDefinition" );
        
        bd.setTemplate( true );
        
        return addBuildDefinitionInTemplate( template, bd, true );
    }

    public BuildDefinitionTemplate getDefaultShellBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );
        if ( template != null )
        {
            return template;
        }
        getLogger().info( "create default ShellBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "default shell template" );
        template.setType( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR  );
        
        template = addBuildDefinitionTemplate( template );        
        
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );

        bd.setTemplate( true );
        
        bd.setDescription( "default shell buildDefinition" );
        
        return addBuildDefinitionInTemplate( template, bd, true );
    }
    
    private Schedule getDefaultSchedule()
        throws BuildDefinitionServiceException
    {
        try
        {
            return configurationService.getDefaultSchedule();
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    
    // ------------------------------------------------------
    //  BuildDefinitionTemplate
    // ------------------------------------------------------    

    public List<BuildDefinitionTemplate> getAllBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getAllBuildDefinitionTemplate();
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public BuildDefinitionTemplate getBuildDefinitionTemplate( int id )
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getBuildDefinitionTemplate( id );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public void removeBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException
    {
        try
        {
            // first remove links to buildDefs
            // TODO in the same db transaction ?
            buildDefinitionTemplate.setBuildDefinitions( null );
            buildDefinitionTemplate = store.updateBuildDefinitionTemplate( buildDefinitionTemplate );
            store.removeBuildDefinitionTemplate( buildDefinitionTemplate );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public BuildDefinitionTemplate updateBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException
    {
        try
        {
            BuildDefinitionTemplate stored = getBuildDefinitionTemplate( buildDefinitionTemplate.getId() );
            stored.setName( buildDefinitionTemplate.getName() );
            stored.setBuildDefinitions( buildDefinitionTemplate.getBuildDefinitions() );
            return store.updateBuildDefinitionTemplate( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public BuildDefinitionTemplate addBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.addBuildDefinitionTemplate( buildDefinitionTemplate );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }
    
    public BuildDefinitionTemplate addBuildDefinitionInTemplate( BuildDefinitionTemplate buildDefinitionTemplate,
                                                                 BuildDefinition buildDefinition, boolean template )
        throws BuildDefinitionServiceException
    {
        try
        {
            BuildDefinitionTemplate stored = getBuildDefinitionTemplate( buildDefinitionTemplate.getId() );
            stored.setName( buildDefinitionTemplate.getName() );
            BuildDefinition storedBuildDefinition = getBuildDefinition( buildDefinition.getId() );
            if (storedBuildDefinition != null)
            {
                buildDefinition = storedBuildDefinition;
            }
            buildDefinition.setTemplate( template );
            //stored.addBuildDefinition( addBuildDefinition( buildDefinition ) );
            stored.addBuildDefinition( buildDefinition );
            return store.updateBuildDefinitionTemplate( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }    

    public BuildDefinitionTemplate removeBuildDefinitionFromTemplate( BuildDefinitionTemplate buildDefinitionTemplate,
                                                                      BuildDefinition buildDefinition )
        throws BuildDefinitionServiceException
    {
        try
        {
            BuildDefinitionTemplate stored = getBuildDefinitionTemplate( buildDefinitionTemplate.getId() );
            stored.setName( buildDefinitionTemplate.getName() );
            List<BuildDefinition> buildDefinitions = new ArrayList<BuildDefinition>();
            for (int i = 0,size = stored.getBuildDefinitions().size();i<size;i++)
            {
                BuildDefinition buildDef = (BuildDefinition) stored.getBuildDefinitions().get( i );
                if ( buildDef.getId() != buildDefinition.getId() )
                {
                    buildDefinitions.add( getBuildDefinition( buildDef.getId() ) );
                }                
            }
            stored.setBuildDefinitions( buildDefinitions );
            return store.updateBuildDefinitionTemplate( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }

    }

    public void addTemplateInProject( int buildDefinitionTemplateId, Project project )
        throws BuildDefinitionServiceException
    {
        try
        {
            BuildDefinitionTemplate template = getBuildDefinitionTemplate( buildDefinitionTemplateId );
            if ( template.getBuildDefinitions().isEmpty() )
            {
                return;
            }
            project = store.getProjectWithBuildDetails( project.getId() );
            List<BuildDefinition> buildDefs = new ArrayList<BuildDefinition>();
            for ( Iterator<BuildDefinition> iterator = template.getBuildDefinitions().iterator(); iterator.hasNext(); )
            {
                BuildDefinition bd = iterator.next();
                bd = cloneBuildDefinition( bd );
                bd.setTemplate( false );
                bd = store.addBuildDefinition( bd );
                project.addBuildDefinition( bd );
            }
            store.updateProject( project );

        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public ProjectGroup addBuildDefinitionTemplateToProjectGroup( int projectGroupId,
                                                                             BuildDefinitionTemplate template )
        throws BuildDefinitionServiceException, ContinuumObjectNotFoundException
    {
        try
        {
            ProjectGroup projectGroup = store.getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );
            if ( template.getBuildDefinitions().isEmpty() )
            {
                return null;
            }
            List<BuildDefinition> buildDefs = new ArrayList<BuildDefinition>();
            for ( Iterator<BuildDefinition> iterator = template.getBuildDefinitions().iterator(); iterator.hasNext(); )
            {
                BuildDefinition bd = iterator.next();
                bd = store.addBuildDefinition( cloneBuildDefinition( bd ) );
                projectGroup.addBuildDefinition( bd );
            }
            store.updateProjectGroup( projectGroup );
            return projectGroup;

        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }
    
    public List<BuildDefinitionTemplate> getBuildDefinitionTemplatesWithType( String type )
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getBuildDefinitionTemplatesWithType( type );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public List<BuildDefinitionTemplate> getContinuumBuildDefinitionTemplates()
        throws BuildDefinitionServiceException
    {
        try
        {
            return store.getContinuumBuildDefinitionTemplates();
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }
}
