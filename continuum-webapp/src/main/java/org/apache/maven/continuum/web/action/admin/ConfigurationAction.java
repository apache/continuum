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

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.apache.continuum.configuration.ContinuumConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Preparable;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="configuration"
 */
public class ConfigurationAction
    extends ContinuumActionSupport
    implements Preparable, SecureAction
{
    private Logger logger = LoggerFactory.getLogger( this.getClass() );

    private String workingDirectory;

    private String buildOutputDirectory;

    private String deploymentRepositoryDirectory;

    private String baseUrl;

    private String releaseOutputDirectory;

    private int numberOfAllowedBuildsinParallel = 1;

    private boolean requireReleaseOutput;

    private boolean distributedBuildEnabled;

    public void prepare()
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        File workingDirectoryFile = configuration.getWorkingDirectory();
        if ( workingDirectoryFile != null )
        {
            workingDirectory = workingDirectoryFile.getAbsolutePath();
        }

        File buildOutputDirectoryFile = configuration.getBuildOutputDirectory();
        if ( buildOutputDirectoryFile != null )
        {
            buildOutputDirectory = buildOutputDirectoryFile.getAbsolutePath();
        }

        File deploymentRepositoryDirectoryFile = configuration.getDeploymentRepositoryDirectory();
        if ( deploymentRepositoryDirectoryFile != null )
        {
            deploymentRepositoryDirectory = deploymentRepositoryDirectoryFile.getAbsolutePath();
        }

        baseUrl = configuration.getUrl();

        if ( StringUtils.isEmpty( baseUrl ) )
        {
            HttpServletRequest request = ServletActionContext.getRequest();
            baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                request.getContextPath();
            logger.info( "baseUrl='" + baseUrl + "'" );
        }

        File releaseOutputDirectoryFile = configuration.getReleaseOutputDirectory();
        if ( releaseOutputDirectoryFile != null )
        {
            releaseOutputDirectory = releaseOutputDirectoryFile.getAbsolutePath();
        }

        numberOfAllowedBuildsinParallel = configuration.getNumberOfBuildsInParallel();

        if ( numberOfAllowedBuildsinParallel == 0 )
        {
            numberOfAllowedBuildsinParallel = 1;
        }

        String requireRelease = ServletActionContext.getRequest().getParameter( "requireReleaseOutput" );
        setRequireReleaseOutput( new Boolean( requireRelease ) );

        distributedBuildEnabled = configuration.isDistributedBuildEnabled();
    }

    public String input()
    {
        if ( isRequireReleaseOutput() )
        {
            addActionError( getText( "configuration.releaseOutputDirectory.required" ) );
        }

        if ( numberOfAllowedBuildsinParallel <= 0 )
        {
            addActionError( getText( "configuration.numberOfBuildsInParallel.invalid" ) );
        }

        return INPUT;
    }

    public String save()
        throws ConfigurationStoringException, ContinuumStoreException, ContinuumConfigurationException
    {
        if ( numberOfAllowedBuildsinParallel <= 0 )
        {
            addActionError( "Number of Allowed Builds in Parallel must be greater than zero." );
            return ERROR;
        }

        ConfigurationService configuration = getContinuum().getConfiguration();

        configuration.setWorkingDirectory( new File( workingDirectory ) );

        configuration.setBuildOutputDirectory( new File( buildOutputDirectory ) );

        configuration.setNumberOfBuildsInParallel( numberOfAllowedBuildsinParallel );

        if ( StringUtils.isNotEmpty( deploymentRepositoryDirectory ) )
        {
            configuration.setDeploymentRepositoryDirectory( new File( deploymentRepositoryDirectory ) );
        }
        else
        {
            configuration.setDeploymentRepositoryDirectory( null );
        }

        configuration.setUrl( baseUrl );

        configuration.setInitialized( true );

        if ( StringUtils.isNotEmpty( releaseOutputDirectory ) )
        {
            configuration.setReleaseOutputDirectory( new File( releaseOutputDirectory ) );
        }
        else if ( isRequireReleaseOutput() )
        {
            addActionError( getText( "configuration.releaseOutputDirectory.required" ) );
            return ERROR;
        }
        else
        {
            configuration.setReleaseOutputDirectory( null );
        }

        configuration.setDistributedBuildEnabled( distributedBuildEnabled );

        configuration.store();

        return SUCCESS;
    }

    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( String workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public String getDeploymentRepositoryDirectory()
    {
        return deploymentRepositoryDirectory;
    }

    public void setDeploymentRepositoryDirectory( String deploymentRepositoryDirectory )
    {
        this.deploymentRepositoryDirectory = deploymentRepositoryDirectory;
    }

    public String getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( String buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_CONFIGURATION, Resource.GLOBAL );

        return bundle;
    }

    public String getReleaseOutputDirectory()
    {
        return releaseOutputDirectory;
    }

    public void setReleaseOutputDirectory( String releaseOutputDirectory )
    {
        this.releaseOutputDirectory = releaseOutputDirectory;
    }

    public boolean isRequireReleaseOutput()
    {
        return requireReleaseOutput;
    }

    public void setRequireReleaseOutput( boolean requireReleaseOutput )
    {
        this.requireReleaseOutput = requireReleaseOutput;
    }

    public int getNumberOfAllowedBuildsinParallel()
    {
        return numberOfAllowedBuildsinParallel;
    }

    public void setNumberOfAllowedBuildsinParallel( int numberOfAllowedBuildsinParallel )
    {
        this.numberOfAllowedBuildsinParallel = numberOfAllowedBuildsinParallel;
    }

    public boolean isDistributedBuildEnabled()
    {
        return distributedBuildEnabled;
    }

    public void setDistributedBuildEnabled( boolean distributedBuildEnabled )
    {
        this.distributedBuildEnabled = distributedBuildEnabled;
    }
}
