package org.apache.maven.continuum.web.action.admin;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.AbstractBuildDefinitionAction;
import org.apache.maven.continuum.web.model.BuildDefinitionSummary;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import com.opensymphony.xwork2.Preparable;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 16 sept. 07
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildDefinitionTemplates"
 */
public class BuildDefinitionTemplateAction
    extends AbstractBuildDefinitionAction
    implements SecureAction, Preparable
{
    private List<BuildDefinitionTemplate> templates;

    private BuildDefinitionTemplate buildDefinitionTemplate;

    private List<String> buildDefinitionTypes;

    private List<BuildDefinitionSummary> buildDefinitionSummaries;

    private BuildDefinition buildDefinition;

    private Collection<Schedule> schedules;

    private List<Profile> profiles;
 
    private List<String> selectedBuildDefinitionIds;
    
    private List<BuildDefinition> buildDefinitions;
    
    // -------------------------------------------------------
    //  Webwork Methods
    // ------------------------------------------------------- 

    public void prepare()
        throws Exception
    {
        super.prepare();
        buildDefinitionTypes = new LinkedList<String>();
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );
        this.setSchedules( getContinuum().getSchedules() );
        this.setProfiles( getContinuum().getProfileService().getAllProfiles() );
        this.setBuildDefinitions( getContinuum().getBuildDefinitionService().getAllTemplates() );
    }

    public String input()
        throws Exception
    {
        return INPUT;
    }

    public String summary()
        throws Exception
    {
        this.templates = getContinuum().getBuildDefinitionService().getAllBuildDefinitionTemplate();
        List<BuildDefinition> buildDefinitions = getContinuum().getBuildDefinitionService().getAllTemplates();
        this.buildDefinitionSummaries = generateBuildDefinitionSummaries( buildDefinitions );
        return SUCCESS;
    }

    public String edit()
        throws Exception
    {
        this.buildDefinitionTemplate = getContinuum().getBuildDefinitionService()
            .getBuildDefinitionTemplate( this.buildDefinitionTemplate.getId() );
        this.setBuildDefinitions( getContinuum().getBuildDefinitionService().getAllTemplates() );
        this.selectedBuildDefinitionIds = new ArrayList<String>();
        if ( this.buildDefinitionTemplate.getBuildDefinitions() != null )
        {
            for ( Iterator<BuildDefinition> iterator = this.buildDefinitionTemplate.getBuildDefinitions().iterator(); iterator
                .hasNext(); )
            {
                this.selectedBuildDefinitionIds.add( Integer.toString( iterator.next().getId() ) );
            }
        }
        List<BuildDefinition> nonUsedBuildDefinitions = new ArrayList<BuildDefinition>();
        for ( BuildDefinition buildDefinition : getBuildDefinitions() )
        {
            if ( !getSelectedBuildDefinitionIds().contains( Integer.toString( buildDefinition.getId() ) ) )
            {
                nonUsedBuildDefinitions.add( buildDefinition );
            }
        }
        this.setBuildDefinitions( nonUsedBuildDefinitions );
        return SUCCESS;
    }

    public String save()
        throws Exception
    {
        List<BuildDefinition> selectedBuildDefinitions = getBuildDefinitionsFromSelectedBuildDefinitions();
        if ( this.buildDefinitionTemplate.getId() > 0 )
        {
            buildDefinitionTemplate.setBuildDefinitions( selectedBuildDefinitions );
            this.getContinuum().getBuildDefinitionService().updateBuildDefinitionTemplate( buildDefinitionTemplate );
        }
        else
        {
            buildDefinitionTemplate.setBuildDefinitions( selectedBuildDefinitions );
            this.buildDefinitionTemplate = this.getContinuum().getBuildDefinitionService()
                .addBuildDefinitionTemplate( buildDefinitionTemplate );
        }

        return SUCCESS;
    }

    public String delete()
        throws BuildDefinitionServiceException
    {
        buildDefinitionTemplate = getContinuum().getBuildDefinitionService()
            .getBuildDefinitionTemplate( this.buildDefinitionTemplate.getId() );
        this.getContinuum().getBuildDefinitionService().removeBuildDefinitionTemplate( buildDefinitionTemplate );
        return SUCCESS;
    }
    
    private List<BuildDefinition> getBuildDefinitionsFromSelectedBuildDefinitions()
        throws ContinuumException
    {
        if ( this.selectedBuildDefinitionIds == null )
        {
            return Collections.EMPTY_LIST;
        }
        List<BuildDefinition> selectedBuildDefinitions = new ArrayList<BuildDefinition>();
        for ( String selectedBuildDefinitionId : selectedBuildDefinitionIds )
        {
            BuildDefinition buildDefinition = getContinuum()
                .getBuildDefinition( Integer.parseInt( selectedBuildDefinitionId ) );
            selectedBuildDefinitions.add( buildDefinition );
        }
        return selectedBuildDefinitions;
    }

    // -----------------------------------------------------
    //  BuildDefinition
    // -----------------------------------------------------

    public String inputBuildDefinition()
    {
        return INPUT;
    }
    
    public String editBuildDefinition()
        throws Exception
    {
        this.buildDefinition = getContinuum().getBuildDefinitionService().getBuildDefinition(
                                                                                              this.buildDefinition
                                                                                                  .getId() );
        return SUCCESS;
    }

    public String saveBuildDefinition()
        throws Exception
    {
        if ( buildDefinition.getProfile() != null )
        {
            Profile profile = getContinuum().getProfileService().getProfile( buildDefinition.getProfile().getId() );
            if ( profile != null )
            {
                buildDefinition.setProfile( profile );
            }
            else
            {
                buildDefinition.setProfile( null );
            }
        }
        if ( buildDefinition.getSchedule() != null )
        {
            if ( buildDefinition.getSchedule().getId() > 0 )
            {
                buildDefinition.setSchedule( getContinuum().getSchedule( buildDefinition.getSchedule().getId() ) );
            }
        }
        
        
        if ( this.buildDefinition.getId() > 0 )
        {
            this.getContinuum().getBuildDefinitionService().updateBuildDefinition( buildDefinition );
        }
        else
        {
            this.buildDefinition = this.getContinuum().getBuildDefinitionService().addBuildDefinition( buildDefinition );
        }

        return SUCCESS;
    }

    public String deleteBuildDefinition()
        throws BuildDefinitionServiceException
    {
        buildDefinition = getContinuum().getBuildDefinitionService().getBuildDefinition( this.buildDefinition.getId() );
        this.getContinuum().getBuildDefinitionService().removeBuildDefinition( buildDefinition );
        return SUCCESS;
    }

    // -----------------------------------------------------
    // security
    // -----------------------------------------------------    

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_BUILD_TEMPLATES, Resource.GLOBAL );

        return bundle;
    }

    // -------------------------------------------------------
    // Webwork setter/getter
    // -------------------------------------------------------    

    public BuildDefinitionTemplate getBuildDefinitionTemplate()
    {
        if (buildDefinitionTemplate == null)
        {
            this.buildDefinitionTemplate = new BuildDefinitionTemplate();
        }
        return buildDefinitionTemplate;
    }

    public void setBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
    {
        this.buildDefinitionTemplate = buildDefinitionTemplate;
    }

    public List<String> getBuildDefinitionTypes()
    {
        return buildDefinitionTypes;
    }

    public void setBuildDefinitionTypes( List<String> buildDefinitionTypes )
    {
        this.buildDefinitionTypes = buildDefinitionTypes;
    }

    public List<BuildDefinitionTemplate> getTemplates()
    {
        return templates;
    }

    public void setTemplates( List<BuildDefinitionTemplate> templates )
    {
        this.templates = templates;
    }

    public List<BuildDefinitionSummary> getBuildDefinitionSummaries()
    {
        return buildDefinitionSummaries;
    }

    public void setBuildDefinitionSummaries( List<BuildDefinitionSummary> buildDefinitionSummaries )
    {
        this.buildDefinitionSummaries = buildDefinitionSummaries;
    }

    public BuildDefinition getBuildDefinition()
    {
        if ( this.buildDefinition == null )
        {
            this.buildDefinition = new BuildDefinition();
        }
        return buildDefinition;
    }

    public void setBuildDefinition( BuildDefinition buildDefinition )
    {
        this.buildDefinition = buildDefinition;
    }

    public List<Profile> getProfiles()
    {
        return profiles;
    }

    public void setProfiles( List<Profile> profiles )
    {
        this.profiles = profiles;
    }

    public void setSchedules( Collection<Schedule> schedules )
    {
        this.schedules = schedules;
    }

    public Collection<Schedule> getSchedules()
    {
        return schedules;
    }
    
    public List<BuildDefinition> getBuildDefinitions()
    {
        return buildDefinitions;
    }

    public void setBuildDefinitions( List<BuildDefinition> buildDefinitions )
    {
        this.buildDefinitions = buildDefinitions;
    }

    public List<String> getSelectedBuildDefinitionIds()
    {
        return selectedBuildDefinitionIds == null ? Collections.EMPTY_LIST : selectedBuildDefinitionIds;
    }

    public void setSelectedBuildDefinitionIds( List<String> selectedBuildDefinitionIds )
    {
        this.selectedBuildDefinitionIds = selectedBuildDefinitionIds;
    }

}
