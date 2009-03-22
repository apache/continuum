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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.model.repository.AbstractPurgeConfiguration;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.repository.RepositoryService;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import com.opensymphony.xwork2.Preparable;

/**
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="purgeConfiguration"
 *
 */
public class PurgeConfigurationAction
    extends ContinuumConfirmAction
    implements Preparable, SecureAction
{
    private static final String PURGE_TYPE_REPOSITORY = "repository";
    
    private static final String PURGE_TYPE_DIRECTORY = "directory";
    
    private static final String PURGE_DIRECTORY_RELEASES = "releases";
    
    private static final String PURGE_DIRECTORY_BUILDOUTPUT = "buildOutput";
    
    private static final int DEFAULT_RETENTION_COUNT = 2;
    
    private static final int DEFAULT_DAYS_OLDER = 100;
    
    private String purgeType;
    
    private String directoryType;
    
    private String description;
    
    private String message;
    
    private boolean deleteAll;
    
    private boolean deleteReleasedSnapshots;
    
    private boolean enabled;
    
    private boolean confirmed;
    
    private boolean defaultPurgeConfiguration;
    
    private int retentionCount;
    
    private int daysOlder;
    
    private int repositoryId;
    
    private int scheduleId;
    
    private int purgeConfigId;
    
    private AbstractPurgeConfiguration purgeConfig;
    
    private Map<Integer, String> repositories;
    
    private Map<Integer, String> schedules;
    
    private List<RepositoryPurgeConfiguration> repoPurgeConfigs;
    
    private List<DirectoryPurgeConfiguration> dirPurgeConfigs;
    
    private List<String> directoryTypes;
    
    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigService;
    
    /**
     * @plexus.requirement
     */
    private RepositoryService repositoryService;
    
    public void prepare()
        throws Exception
    {
        super.prepare();
        
        // build schedules
        if ( schedules == null )
        {
            schedules = new HashMap<Integer, String>();

            Collection<Schedule> allSchedules = getContinuum().getSchedules();

            for ( Schedule schedule : allSchedules )
            {
                schedules.put( new Integer( schedule.getId() ), schedule.getName() );
            }
        }
        
        // build repositories
        if ( repositories == null )
        {
            repositories = new HashMap<Integer, String>();
            
            List<LocalRepository> allRepositories = repositoryService.getAllLocalRepositories();
            
            for ( LocalRepository repository : allRepositories )
            {
                repositories.put( new Integer( repository.getId() ), repository.getName() );
            }
        }
        
        directoryTypes = new ArrayList<String>();
        directoryTypes.add( PURGE_DIRECTORY_RELEASES );
        directoryTypes.add( PURGE_DIRECTORY_BUILDOUTPUT );
    }
    
    public String input()
        throws Exception
    {
        if ( purgeConfigId != 0 )
        {
            purgeConfig = purgeConfigService.getPurgeConfiguration( purgeConfigId );
            
            if ( purgeConfig instanceof RepositoryPurgeConfiguration )
            {
                RepositoryPurgeConfiguration repoPurge = (RepositoryPurgeConfiguration) purgeConfig;

                this.purgeType = PURGE_TYPE_REPOSITORY;
                this.daysOlder = repoPurge.getDaysOlder();
                this.retentionCount = repoPurge.getRetentionCount();
                this.deleteAll = repoPurge.isDeleteAll();
                this.deleteReleasedSnapshots = repoPurge.isDeleteReleasedSnapshots();
                this.enabled = repoPurge.isEnabled();
                this.defaultPurgeConfiguration = repoPurge.isDefaultPurge();
                this.description = repoPurge.getDescription();
                
                if ( repoPurge.getRepository() != null )
                {
                    this.repositoryId = repoPurge.getRepository().getId();
                }
                
                if ( repoPurge.getSchedule() != null )
                {
                    this.scheduleId = repoPurge.getSchedule().getId();
                }
            }
            else if ( purgeConfig instanceof DirectoryPurgeConfiguration )
            {
                DirectoryPurgeConfiguration dirPurge = (DirectoryPurgeConfiguration) purgeConfig;
                
                this.purgeType = PURGE_TYPE_DIRECTORY;
                this.daysOlder = dirPurge.getDaysOlder();
                this.retentionCount = dirPurge.getRetentionCount();
                this.directoryType = dirPurge.getDirectoryType();
                this.deleteAll = dirPurge.isDeleteAll();
                this.enabled = dirPurge.isEnabled();
                this.defaultPurgeConfiguration = dirPurge.isDefaultPurge();
                this.description = dirPurge.getDescription();
                
                if ( dirPurge.getSchedule() != null )
                {
                    this.scheduleId = dirPurge.getSchedule().getId();
                }
            }
        }
        else
        {
            this.retentionCount = DEFAULT_RETENTION_COUNT;
            this.daysOlder = DEFAULT_DAYS_OLDER;
        }
        
        return INPUT;
    }
    
    public String list()
        throws Exception
    {
        String errorMessage = ServletActionContext.getRequest().getParameter( "errorMessage" );
        
        if ( errorMessage != null )
        {
            addActionError( getText( errorMessage ) );
        }
        
        repoPurgeConfigs = purgeConfigService.getAllRepositoryPurgeConfigurations();
        dirPurgeConfigs = purgeConfigService.getAllDirectoryPurgeConfigurations();
        
        return SUCCESS;
    }
    
    public String save()
        throws Exception
    {
        if ( purgeConfigId == 0 )
        {
            if ( purgeType.equals( PURGE_TYPE_REPOSITORY ) )
            {
                purgeConfig = new RepositoryPurgeConfiguration();
            }
            else
            {
                purgeConfig = new DirectoryPurgeConfiguration();
            }
            
            purgeConfig = setupPurgeConfiguration( purgeConfig );
            
            purgeConfig = purgeConfigService.addPurgeConfiguration( purgeConfig );
        }
        else
        {
            purgeConfig = purgeConfigService.getPurgeConfiguration( purgeConfigId );
            purgeConfig = setupPurgeConfiguration( purgeConfig );
            
            purgeConfigService.updatePurgeConfiguration( purgeConfig );
        }
        
        if ( purgeConfig.isDefaultPurge() )
        {
            updateDefaultPurgeConfiguration();
        }
        
        return SUCCESS;
    }
    
    public String remove()
        throws Exception
    {
        if ( confirmed )
        {
        	purgeConfigService.removePurgeConfiguration( purgeConfigId );
        }
        else
        {
            return CONFIRM;
        }
        
        return SUCCESS;
    }
    
    public String purge()
        throws Exception
    {
        ContinuumPurgeManager purgeManager = getContinuum().getPurgeManager();
        TaskQueueManager taskQueueManager = getContinuum().getTaskQueueManager();
        
        if ( purgeConfigId > 0 )
        {
            purgeConfig = purgeConfigService.getPurgeConfiguration( purgeConfigId );
            
            if ( purgeConfig instanceof RepositoryPurgeConfiguration )
            {
                RepositoryPurgeConfiguration repoPurge = (RepositoryPurgeConfiguration) purgeConfig;
                
                // check if repository is in use
                if ( taskQueueManager.isRepositoryInUse( repoPurge.getRepository().getId() ) )
                {
                    message = "repository.error.purge.in.use";
                    return ERROR;
                }
                
                purgeManager.purgeRepository( repoPurge );
            }
            else
            {
                DirectoryPurgeConfiguration dirPurge = (DirectoryPurgeConfiguration) purgeConfig;
                purgeManager.purgeDirectory( dirPurge );
            }
        }
        
        return SUCCESS;
    }
    
    public String getPurgeType()
    {
        return this.purgeType;
    }
    
    public void setPurgeType( String purgeType )
    {
        this.purgeType = purgeType;
    }
    
    public String getDirectoryType()
    {
        return this.directoryType;
    }
    
    public void setDirectoryType( String directoryType )
    {
        this.directoryType = directoryType;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public void setDescription( String description )
    {
        this.description = description;
    }
    
    public String getMessage()
    {
        return this.message;
    }
    
    public void setMessage( String message )
    {
        this.message = message;
    }
    
    public boolean isDeleteAll()
    {
        return this.deleteAll;
    }
    
    public void setDeleteAll( boolean deleteAll )
    {
        this.deleteAll = deleteAll;
    }
    
    public boolean isDeleteReleasedSnapshots()
    {
        return this.deleteReleasedSnapshots;
    }
    
    public void setDeleteReleasedSnapshots( boolean deleteReleasedSnapshots )
    {
        this.deleteReleasedSnapshots = deleteReleasedSnapshots;
    }
    
    public boolean isEnabled()
    {
        return this.enabled;
    }
    
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }
    
    public boolean isConfirmed()
    {
        return this.confirmed;
    }
    
    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }
    
    public boolean isDefaultPurgeConfiguration()
    {
        return this.defaultPurgeConfiguration;
    }
    
    public void setDefaultPurgeConfiguration( boolean defaultPurgeConfiguration )
    {
        this.defaultPurgeConfiguration = defaultPurgeConfiguration;
    }
    
    public int getRetentionCount()
    {
        return this.retentionCount;
    }
    
    public void setRetentionCount( int retentionCount )
    {
        this.retentionCount = retentionCount;
    }
    
    public int getDaysOlder()
    {
        return this.daysOlder;
    }
    
    public void setDaysOlder( int daysOlder )
    {
        this.daysOlder = daysOlder;
    }
    
    public int getRepositoryId()
    {
        return this.repositoryId;
    }
    
    public void setRepositoryId( int repositoryId )
    {
        this.repositoryId = repositoryId;
    }
    
    public int getScheduleId()
    {
        return this.scheduleId;
    }
    
    public void setScheduleId( int scheduleId )
    {
        this.scheduleId = scheduleId;
    }
    
    public int getPurgeConfigId()
    {
        return purgeConfigId;
    }
    
    public void setPurgeConfigId( int purgeConfigId )
    {
        this.purgeConfigId = purgeConfigId;
    }
    
    public AbstractPurgeConfiguration getPurgeConfig()
    {
        return this.purgeConfig;
    }
    
    public void setPurgeConfig( AbstractPurgeConfiguration purgeConfig )
    {
        this.purgeConfig = purgeConfig;
    }
    
    public Map<Integer, String> getRepositories()
    {
        return this.repositories;
    }
    
    public void setRepositories( Map<Integer, String> repositories )
    {
        this.repositories = repositories;
    }
    
    public Map<Integer, String> getSchedules()
    {
        return this.schedules;
    }
    
    public void setSchedules( Map<Integer, String> schedules )
    {
        this.schedules = schedules;
    }
    
    public List<RepositoryPurgeConfiguration> getRepoPurgeConfigs()
    {
        return this.repoPurgeConfigs;
    }
    
    public void setRepoPurgeConfigs( List<RepositoryPurgeConfiguration> repoPurgeConfigs )
    {
        this.repoPurgeConfigs = repoPurgeConfigs;
    }
    
    public List<DirectoryPurgeConfiguration> getDirPurgeConfigs()
    {
        return this.dirPurgeConfigs;
    }
    
    public void setDirPurgeConfigs( List<DirectoryPurgeConfiguration> dirPurgeConfigs )
    {
        this.dirPurgeConfigs = dirPurgeConfigs;
    }
    
    public List<String> getDirectoryTypes()
    {
        return this.directoryTypes;
    }
    
    public void setDirectoryTypes( List<String> directoryTypes )
    {
        this.directoryTypes = directoryTypes;
    }
    
    private AbstractPurgeConfiguration setupPurgeConfiguration( AbstractPurgeConfiguration purgeConfiguration )
        throws Exception
    {
        if ( purgeConfiguration instanceof RepositoryPurgeConfiguration )
        {
            return buildRepoPurgeConfiguration();
        }
        else
        {
            return buildDirPurgeConfiguration();
        }
    }
    
    private RepositoryPurgeConfiguration buildRepoPurgeConfiguration()
        throws Exception
    {
        RepositoryPurgeConfiguration repoPurge = (RepositoryPurgeConfiguration) purgeConfig;
        repoPurge.setDeleteAll( this.deleteAll );
        repoPurge.setDeleteReleasedSnapshots( this.deleteReleasedSnapshots );
        repoPurge.setDaysOlder( this.daysOlder );
        repoPurge.setRetentionCount( this.retentionCount );
        repoPurge.setEnabled( this.enabled );
        repoPurge.setDefaultPurge( this.defaultPurgeConfiguration );
        repoPurge.setDescription( this.description );
        repoPurge.setDefaultPurge( this.defaultPurgeConfiguration );
        
        if ( repositoryId != 0 )
        {
            LocalRepository repository = repositoryService.getLocalRepository( repositoryId );
            repoPurge.setRepository( repository );
        }
        
        if ( scheduleId > 0 )
        {
            Schedule schedule = getContinuum().getSchedule( scheduleId );
            repoPurge.setSchedule( schedule );
        }
        
        return repoPurge;
    }
    
    private DirectoryPurgeConfiguration buildDirPurgeConfiguration()
        throws Exception
    {
        DirectoryPurgeConfiguration dirPurge = (DirectoryPurgeConfiguration) purgeConfig;
        dirPurge.setDeleteAll( this.deleteAll );
        dirPurge.setEnabled( this.enabled );
        dirPurge.setDaysOlder( this.daysOlder );
        dirPurge.setRetentionCount( this.retentionCount );
        dirPurge.setDescription( this.description );
        dirPurge.setDirectoryType( this.directoryType );
        dirPurge.setDefaultPurge( this.defaultPurgeConfiguration );
        
        if ( scheduleId > 0 )
        {
            Schedule schedule = getContinuum().getSchedule( scheduleId );
            dirPurge.setSchedule( schedule );
        }
        
        ConfigurationService configService = getContinuum().getConfiguration();
        String path = null;
        
        if ( this.directoryType.equals( PURGE_DIRECTORY_RELEASES ) )
        {
            path = configService.getWorkingDirectory().getAbsolutePath();
        }
        else if ( this.directoryType.equals( PURGE_DIRECTORY_BUILDOUTPUT ) )
        {
            path = configService.getBuildOutputDirectory().getAbsolutePath();
        }
        
        dirPurge.setLocation( path );
        
        return dirPurge;
    }
    
    private void updateDefaultPurgeConfiguration()
        throws Exception
    {
        if ( purgeConfig instanceof RepositoryPurgeConfiguration )
        {
            RepositoryPurgeConfiguration repoPurge = purgeConfigService.getDefaultPurgeConfigurationForRepository( repositoryId );
            
            if ( repoPurge != null && repoPurge.getId() != purgeConfig.getId() )
            {
                repoPurge.setDefaultPurge( false );
                purgeConfigService.updateRepositoryPurgeConfiguration( repoPurge );
            }
        }
        else if ( purgeConfig instanceof DirectoryPurgeConfiguration )
        {
            DirectoryPurgeConfiguration dirPurge = purgeConfigService.getDefaultPurgeConfigurationForDirectoryType( directoryType );
            
            if ( dirPurge != null && dirPurge.getId() != purgeConfig.getId() )
            {
                dirPurge.setDefaultPurge( false );
                purgeConfigService.updateDirectoryPurgeConfiguration( dirPurge );
            }
        }
    }
    
    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_PURGING, Resource.GLOBAL );

        return bundle;
    }
}
