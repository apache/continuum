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

import com.opensymphony.xwork2.Preparable;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.repository.RepositoryService;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( role = com.opensymphony.xwork2.Action.class, hint = "purge", instantiationStrategy = "per-lookup"  )
public class PurgeAction
    extends ContinuumConfirmAction
    implements Preparable, SecureAction
{
    private static final Logger logger = LoggerFactory.getLogger( PurgeConfigurationAction.class );

    private static final String DISTRIBUTED_BUILD_SUCCESS = "distributed-build-success";

    private static final String PURGE_TYPE_REPOSITORY = "repository";

    private static final String PURGE_TYPE_DIRECTORY = "directory";

    private static final String PURGE_DIRECTORY_RELEASES = "releases";

    private static final String PURGE_DIRECTORY_BUILDOUTPUT = "buildOutput";

    private static final String PURGE_DIRECTORY_WORKING = "working";

    private Map<Integer, String> repositories;

    private Map<Integer, String> schedules;

    private List<RepositoryPurgeConfiguration> repoPurgeConfigs;

    private List<DirectoryPurgeConfiguration> dirPurgeConfigs;

    private List<DistributedDirectoryPurgeConfiguration> distributedDirPurgeConfigs;

    private List<String> directoryTypes;

    @Requirement
    private PurgeConfigurationService purgeConfigService;

    @Requirement
    private RepositoryService repositoryService;

    private Schedule sched = null;

    public String display()
        throws Exception
    {
        if ( schedules == null )
        {
            schedules = new HashMap<Integer, String>();

            Collection<Schedule> allSchedules = getContinuum().getSchedules();

            for ( Schedule schedule : allSchedules )
            {
                schedules.put( schedule.getId(), schedule.getName() );
            }
        }
        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            String errorMessage = ServletActionContext.getRequest().getParameter( "errorMessage" );

            if ( errorMessage != null )
            {
                addActionError( getText( errorMessage ) );
            }
            distributedDirPurgeConfigs = purgeConfigService.getAllDistributedDirectoryPurgeConfigurations();

            return DISTRIBUTED_BUILD_SUCCESS;
        }
        else
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
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_PURGING, Resource.GLOBAL );

        return bundle;
    }

    public Map<Integer, String> getRepositories()
    {
        return repositories;
    }

    public void setRepositories( Map<Integer, String> repositories )
    {
        this.repositories = repositories;
    }

    public List<RepositoryPurgeConfiguration> getRepoPurgeConfigs()
    {
        return repoPurgeConfigs;
    }

    public void setRepoPurgeConfigs( List<RepositoryPurgeConfiguration> repoPurgeConfigs )
    {
        this.repoPurgeConfigs = repoPurgeConfigs;
    }

    public List<DirectoryPurgeConfiguration> getDirPurgeConfigs()
    {
        return dirPurgeConfigs;
    }

    public void setDirPurgeConfigs( List<DirectoryPurgeConfiguration> dirPurgeConfigs )
    {
        this.dirPurgeConfigs = dirPurgeConfigs;
    }

    public List<DistributedDirectoryPurgeConfiguration> getDistributedDirPurgeConfigs()
    {
        return distributedDirPurgeConfigs;
    }

    public void setDistributedDirPurgeConfigs( List<DistributedDirectoryPurgeConfiguration> distributedDirPurgeConfigs )
    {
        this.distributedDirPurgeConfigs = distributedDirPurgeConfigs;
    }

    public List<String> getDirectoryTypes()
    {
        return directoryTypes;
    }

    public void setDirectoryTypes( List<String> directoryTypes )
    {
        this.directoryTypes = directoryTypes;
    }

    public PurgeConfigurationService getPurgeConfigService()
    {
        return purgeConfigService;
    }

    public void setPurgeConfigService( PurgeConfigurationService purgeConfigService )
    {
        this.purgeConfigService = purgeConfigService;
    }
}
