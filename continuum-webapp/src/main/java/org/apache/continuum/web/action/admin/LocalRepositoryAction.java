package org.apache.continuum.web.action.admin;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.repository.RepositoryService;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import com.opensymphony.xwork2.Preparable;

/**
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="localRepository"
 */
public class LocalRepositoryAction
    extends ContinuumConfirmAction
    implements Preparable, SecureAction
{
    private static final String LAYOUT_DEFAULT = "default";
    
    private static final String LAYOUT_LEGACY = "legacy";
    
    private boolean confirmed;

    private boolean defaultRepo;
    
    private LocalRepository repository;
    
    private List<LocalRepository> repositories;
    
    private List<ProjectGroup> groups;
    
    private List<String> layouts;
    
    private Map<String, Boolean> defaultPurgeMap;

    /**
     * @plexus.requirement
     */
    private RepositoryService repositoryService;
    
    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigService;
    
    public void prepare()
        throws Exception
    {
        super.prepare();
        
        layouts = new ArrayList<String>();
        layouts.add( LAYOUT_DEFAULT );
        layouts.add( LAYOUT_LEGACY );
    }
    
    public String input()
        throws Exception
    {
        defaultRepo = false;
        
        if ( repository != null && repository.getId() > 0 )
        {
            repository = repositoryService.getLocalRepository( repository.getId() );
            
            if ( repository.getName().equals( "DEFAULT" ) )
            {
                defaultRepo = true;
            }
        }
        
        return INPUT;
    }
    
    public String list()
        throws Exception
    {
        repositories = repositoryService.getAllLocalRepositories();
        
        defaultPurgeMap = new HashMap<String, Boolean>();
        
        for ( LocalRepository repo : repositories )
        {
            // get default purge config of repository
            RepositoryPurgeConfiguration purgeConfig = purgeConfigService.getDefaultPurgeConfigurationForRepository( repo.getId() );
            
            if ( purgeConfig == null )
            {
                defaultPurgeMap.put(  repo.getName(), Boolean.FALSE );
            }
            else
            {
                defaultPurgeMap.put(  repo.getName(), Boolean.TRUE );
            }
        }
        
        return SUCCESS;
    }
    
    public String save()
        throws Exception
    {
        List<LocalRepository> allRepositories = repositoryService.getAllLocalRepositories();
        
        for( LocalRepository repo : allRepositories )
        {
            if ( repository.getId() != repo.getId() )
            {
                if ( repository.getName().equals( repo.getName() ) )
                {
                    addActionError( getText( "repository.error.name.unique" ) );
                }
                
                if ( repository.getLocation().equals( repo.getLocation() ) )
                {
                    addActionError( getText( "repository.error.location.unique" ) );
                }
            }
        }
        
        if ( repository.getName().trim().equals( "" ) )
        {
            addActionError( getText( "repository.error.name.cannot.be.spaces" ) );
        }
        
        if ( repository.getLocation().trim().equals( "" ) )
        {
            addActionError( getText( "repository.error.location.cannot.be.spaces" ) );
        }
        
        if ( hasActionErrors() )
        {
            return INPUT;
        }
        
        if ( repository.getId() == 0 )
        {
            repository = repositoryService.addLocalRepository( repository );
            
            createDefaultPurgeConfiguration();
        }
        else
        {
            // check if repository is in use
            TaskQueueManager taskQueueManager = getContinuum().getTaskQueueManager();
            if ( taskQueueManager.isRepositoryInUse( repository.getId() ) )
            {
                addActionError( getText( "repository.error.save.in.use" ) );
                return ERROR;
            }
            
            LocalRepository retrievedRepo = repositoryService.getLocalRepository( repository.getId() );
            
            retrievedRepo.setName( repository.getName() );
            retrievedRepo.setLocation( repository.getLocation() );
            retrievedRepo.setLayout( repository.getLayout() );
            
            repositoryService.updateLocalRepository( retrievedRepo );
        }
        
        return SUCCESS;
    }
    
    public String remove()
        throws Exception
    {
        TaskQueueManager taskQueueManager = getContinuum().getTaskQueueManager();

        repository = repositoryService.getLocalRepository( repository.getId() );

        if ( taskQueueManager.isRepositoryInUse( repository.getId() ) )
        {
            addActionError( getText( "repository.error.remove.in.use", "Unable to remove local repository because it is in use" ) );
        }

        if ( repository.getName().equals( "DEFAULT" ) )
        {
            addActionError( getText( "repository.error.remove.default", "Unable to remove default local repository" ) );
        }

        if ( !hasActionErrors() )
        {
            if ( confirmed )
            {
                repositoryService.removeLocalRepository( repository.getId() );
            }
            else
            {
                return CONFIRM;
            }
        }

        return SUCCESS;
    }
    
    public String doPurge()
        throws Exception
    {
        ContinuumPurgeManager purgeManager = getContinuum().getPurgeManager();
        TaskQueueManager taskQueueManager = getContinuum().getTaskQueueManager();

        // check if repository is in use
        if ( taskQueueManager.isRepositoryInUse( repository.getId() ) )
        {
            addActionError( getText( "repository.error.purge.in.use", "Unable to purge repository because it is in use" ) );
        }
     
        if ( !hasActionErrors() )
        {
            // get default purge configuration for repository
            RepositoryPurgeConfiguration purgeConfig = purgeConfigService.getDefaultPurgeConfigurationForRepository( repository.getId() );
        
            if ( purgeConfig != null )
            {
                purgeManager.purgeRepository( purgeConfig );

                AuditLog event = new AuditLog( "Repository id=" + repository.getId(), AuditLogConstants.PURGE_LOCAL_REPOSITORY );
                event.setCategory( AuditLogConstants.LOCAL_REPOSITORY );
                event.setCurrentUser( getPrincipal() );
                event.log();
            }
        }
        return SUCCESS;
    }
    
    public LocalRepository getRepository()
    {
        return this.repository;
    }
    
    public void setRepository( LocalRepository repository )
    {
        this.repository = repository;
    }
    
    public List<LocalRepository> getRepositories()
    {
        return this.repositories;
    }
    
    public void setRepositories( List<LocalRepository> repositories )
    {
        this.repositories = repositories;
    }
    
    public List<ProjectGroup> getGroups()
    {
        return this.groups;
    }
    
    public void setGroups( List<ProjectGroup> groups )
    {
        this.groups = groups;
    }
    
    public boolean isConfirmed()
    {
        return this.confirmed;
    }
    
    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }
    
    public boolean isDefaultRepo()
    {
        return this.defaultRepo;
    }
    
    public void setDefaultRepo( boolean defaultRepo )
    {
        this.defaultRepo = defaultRepo;
    }
    
    public List<String> getLayouts()
    {
        return this.layouts;
    }
    
    public Map<String, Boolean> getDefaultPurgeMap()
    {
        return this.defaultPurgeMap;
    }
    
    public void setDefaultPurgeMap( Map<String, Boolean> defaultPurgeMap )
    {
        this.defaultPurgeMap = defaultPurgeMap;
    }

    private void createDefaultPurgeConfiguration()
        throws Exception
    {
        RepositoryPurgeConfiguration repoPurge = new RepositoryPurgeConfiguration();
        
        repoPurge.setRepository( repository );
        repoPurge.setDefaultPurge( true );
        
        purgeConfigService.addRepositoryPurgeConfiguration( repoPurge );
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_REPOSITORIES, Resource.GLOBAL );

        return bundle;
    }
}
