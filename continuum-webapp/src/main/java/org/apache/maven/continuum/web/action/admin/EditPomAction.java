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

import com.opensymphony.xwork2.ModelDriven;
import org.apache.continuum.utils.m2.LocalRepositoryHelper;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.continuum.execution.SettingsConfigurationException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.app.company.CompanyPomHandler;
import org.apache.maven.shared.app.configuration.CompanyPom;
import org.apache.maven.shared.app.configuration.Configuration;
import org.apache.maven.shared.app.configuration.MavenAppConfiguration;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.io.IOException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "editPom", instantiationStrategy = "per-lookup" )
public class EditPomAction
    extends ContinuumActionSupport
    implements ModelDriven, SecureAction
{

    @Requirement
    private MavenAppConfiguration appConfiguration;

    @Requirement
    private CompanyPomHandler companyPomHandler;

    private Model companyModel;

    private String organizationLogo;

    @Requirement
    private LocalRepositoryHelper helper;

    public String execute()
        throws IOException, ArtifactInstallationException, SettingsConfigurationException
    {
        if ( organizationLogo != null )
        {
            companyModel.getProperties().setProperty( "organization.logo", organizationLogo );
        }

        companyPomHandler.save( companyModel, helper.getLocalRepository() );

        return SUCCESS;
    }

    public String input()
    {
        return INPUT;
    }

    public Object getModel()
    {
        return companyModel;
    }

    public void prepare()
        throws ProjectBuildingException, ArtifactMetadataRetrievalException, SettingsConfigurationException
    {
        Configuration configuration = appConfiguration.getConfiguration();

        CompanyPom companyPom = configuration.getCompanyPom();
        companyModel = companyPomHandler.getCompanyPomModel( companyPom, helper.getLocalRepository() );

        if ( companyModel == null )
        {
            companyModel = new Model();
            companyModel.setModelVersion( "4.0.0" );
            companyModel.setPackaging( "pom" );

            if ( companyPom != null )
            {
                companyModel.setGroupId( companyPom.getGroupId() );
                companyModel.setArtifactId( companyPom.getArtifactId() );
            }
        }

        organizationLogo = companyModel.getProperties().getProperty( "organization.logo" );
    }

    public String getOrganizationLogo()
    {
        return organizationLogo;
    }

    public void setOrganizationLogo( String organizationLogo )
    {
        this.organizationLogo = organizationLogo;
    }

    public Model getCompanyModel()
    {
        return companyModel;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_CONFIGURATION, Resource.GLOBAL );

        return bundle;
    }
}
