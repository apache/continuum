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

import org.apache.continuum.buildqueue.BuildQueueServiceException;
import org.apache.continuum.configuration.ContinuumConfigurationException;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildDefinitionTemplateDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.builddefinition.BuildDefinitionService"
 * @TODO some cache mechanism ?
 * @since 15 sept. 07
 */
public class DefaultBuildDefinitionService
    implements BuildDefinitionService, Initializable
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildDefinitionService.class );

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
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionTemplateDao buildDefinitionTemplateDao;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private ProjectGroupDao projectGroupDao;

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
            return buildDefinitionDao.getBuildDefinition( buildDefinitionId );
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
            return buildDefinitionDao.addBuildDefinition( buildDefinition );
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
            buildDefinitionDao.removeBuildDefinition( buildDefinition );
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
            BuildDefinition storedBuildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinition.getId() );
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
            storedBuildDefinition.setUpdatePolicy( buildDefinition.getUpdatePolicy() );
            buildDefinitionDao.storeBuildDefinition( storedBuildDefinition );
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
            return buildDefinitionDao.getAllBuildDefinitions();
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
            return buildDefinitionDao.getAllTemplates();
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
        cloned.setUpdatePolicy( buildDefinition.getUpdatePolicy() );
        return cloned;
    }


    public BuildDefinitionTemplate getContinuumDefaultWithType( String type )
        throws BuildDefinitionServiceException
    {
        try
        {
            return buildDefinitionTemplateDao.getContinuumBuildDefinitionTemplateWithType( type );
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public BuildDefinitionTemplate getDefaultAntBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType(
            ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        if ( template != null )
        {
            return template;
        }
        log.info( "create default AntBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "Default Ant Template" );
        template.setType( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );

        template = addBuildDefinitionTemplate( template );

        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( defaultAntGoals );

        bd.setArguments( defaultAntArguments );

        bd.setBuildFile( "build.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setDescription( "Default Ant Build Definition" );

        bd.setTemplate( true );

        bd.setType( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        return addBuildDefinitionInTemplate( template, bd, true );
    }

    public BuildDefinitionTemplate getDefaultMavenOneBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType(
            ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );
        if ( template != null )
        {
            log.debug( "found default maven template " + template.getType() );
            return template;
        }
        log.info( "create default MavenOneBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "Default Maven 1 Template" );
        template.setType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );

        template = addBuildDefinitionTemplate( template );

        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setArguments( defaultM1Arguments );

        bd.setGoals( defaultM1Goals );

        bd.setBuildFile( "project.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );

        bd.setDescription( "Default Maven 1 Build Definition" );

        bd.setTemplate( true );

        return addBuildDefinitionInTemplate( template, bd, true );
    }

    public BuildDefinitionTemplate getDefaultMavenTwoBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType(
            ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        if ( template != null )
        {
            return template;
        }
        log.info( "create default MavenTwoBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "Default Maven Template" );
        template.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );

        template = addBuildDefinitionTemplate( template );

        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( this.defaultM2Goals );

        bd.setArguments( this.defaultM2Arguments );

        bd.setBuildFile( "pom.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );

        bd.setDescription( "Default Maven Build Definition" );

        bd.setTemplate( true );

        return addBuildDefinitionInTemplate( template, bd, true );
    }

    public BuildDefinitionTemplate getDefaultShellBuildDefinitionTemplate()
        throws BuildDefinitionServiceException
    {
        BuildDefinitionTemplate template = getContinuumDefaultWithType(
            ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );
        if ( template != null )
        {
            return template;
        }
        log.info( "create default ShellBuildDefinitionTemplate" );
        template = new BuildDefinitionTemplate();
        template.setContinuumDefault( true );
        template.setName( "Default Shell Template" );
        template.setType( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );

        template = addBuildDefinitionTemplate( template );

        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );

        bd.setTemplate( true );

        bd.setDescription( "Default Shell Build Definition" );

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
        catch ( ContinuumConfigurationException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
        catch ( BuildQueueServiceException e )
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
            return buildDefinitionTemplateDao.getAllBuildDefinitionTemplate();
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
            return buildDefinitionTemplateDao.getBuildDefinitionTemplate( id );
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
            buildDefinitionTemplate = buildDefinitionTemplateDao.updateBuildDefinitionTemplate(
                buildDefinitionTemplate );
            buildDefinitionTemplateDao.removeBuildDefinitionTemplate( buildDefinitionTemplate );
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
            if ( !hasDuplicateTemplateName( buildDefinitionTemplate ) )
            {
                BuildDefinitionTemplate stored = getBuildDefinitionTemplate( buildDefinitionTemplate.getId() );
                stored.setName( buildDefinitionTemplate.getName() );
                stored.setBuildDefinitions( buildDefinitionTemplate.getBuildDefinitions() );
                return buildDefinitionTemplateDao.updateBuildDefinitionTemplate( stored );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }

        return null;
    }

    public BuildDefinitionTemplate addBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException
    {
        try
        {
            if ( !hasDuplicateTemplateName( buildDefinitionTemplate ) )
            {
                return buildDefinitionTemplateDao.addBuildDefinitionTemplate( buildDefinitionTemplate );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }

        return null;
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
            if ( storedBuildDefinition != null )
            {
                buildDefinition = storedBuildDefinition;
            }
            buildDefinition.setTemplate( template );
            //stored.addBuildDefinition( addBuildDefinition( buildDefinition ) );
            stored.addBuildDefinition( buildDefinition );
            return buildDefinitionTemplateDao.updateBuildDefinitionTemplate( stored );
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
            for ( int i = 0, size = stored.getBuildDefinitions().size(); i < size; i++ )
            {
                BuildDefinition buildDef = (BuildDefinition) stored.getBuildDefinitions().get( i );
                if ( buildDef.getId() != buildDefinition.getId() )
                {
                    buildDefinitions.add( getBuildDefinition( buildDef.getId() ) );
                }
            }
            stored.setBuildDefinitions( buildDefinitions );
            return buildDefinitionTemplateDao.updateBuildDefinitionTemplate( stored );
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
            project = projectDao.getProjectWithBuildDetails( project.getId() );

            for ( BuildDefinition bd : (List<BuildDefinition>) template.getBuildDefinitions() )
            {
                bd = cloneBuildDefinition( bd );
                bd.setTemplate( false );
                bd = buildDefinitionDao.addBuildDefinition( bd );
                project.addBuildDefinition( bd );
            }
            projectDao.updateProject( project );

        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    public ProjectGroup addBuildDefinitionTemplateToProjectGroup( int projectGroupId, BuildDefinitionTemplate template )
        throws BuildDefinitionServiceException, ContinuumObjectNotFoundException
    {
        try
        {
            ProjectGroup projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId(
                projectGroupId );
            if ( template.getBuildDefinitions().isEmpty() )
            {
                return null;
            }

            for ( BuildDefinition bd : (List<BuildDefinition>) template.getBuildDefinitions() )
            {
                bd.setTemplate( false );
                bd = buildDefinitionDao.addBuildDefinition( cloneBuildDefinition( bd ) );
                projectGroup.addBuildDefinition( bd );
            }
            projectGroupDao.updateProjectGroup( projectGroup );
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
            return buildDefinitionTemplateDao.getBuildDefinitionTemplatesWithType( type );
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
            return buildDefinitionTemplateDao.getContinuumBuildDefinitionTemplates();
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildDefinitionServiceException( e.getMessage(), e );
        }
    }

    private boolean hasDuplicateTemplateName( BuildDefinitionTemplate buildDefinitionTemplate )
        throws BuildDefinitionServiceException
    {
        boolean isDuplicate = false;
        List<BuildDefinitionTemplate> allBuildDefinitionTemplate = this.getAllBuildDefinitionTemplate();

        for ( BuildDefinitionTemplate template : allBuildDefinitionTemplate )
        {
            String name = buildDefinitionTemplate.getName();
            if ( ( template.getId() != buildDefinitionTemplate.getId() ) && ( template.getName().equals( name ) ) )
            {
                isDuplicate = true;
                break;
            }
        }
        return isDuplicate;
    }
}
