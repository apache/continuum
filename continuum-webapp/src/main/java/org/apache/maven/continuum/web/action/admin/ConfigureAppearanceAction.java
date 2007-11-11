package org.apache.maven.continuum.web.action.admin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.execution.maven.m2.SettingsConfigurationException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.component.AbstractFooterAction;
import org.apache.maven.continuum.web.appareance.AppareanceConfiguration;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.app.company.CompanyPomHandler;
import org.apache.maven.shared.app.configuration.Configuration;
import org.apache.maven.shared.app.configuration.MavenAppConfiguration;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.xwork.interceptor.SecureAction;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionException;
import org.codehaus.plexus.registry.RegistryException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.opensymphony.xwork.ModelDriven;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: ConfigurationAction.java 480950 2006-11-30 14:58:35Z evenisse $
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="configureAppearance"
 */
public class ConfigureAppearanceAction
    extends AbstractFooterAction
    implements ModelDriven, SecureAction
{
    /**
     * @plexus.requirement
     */
    private MavenAppConfiguration appConfiguration;

    /**
     * The configuration.
     */
    private Configuration configuration;

    private Model companyModel;

    /**
     * @plexus.requirement
     */
    private CompanyPomHandler companyPomHandler;

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper helper;

    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder mavenSettingsBuilder;

    /**
     * @plexus.requirement
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @plexus.requirement role-hint="default"
     */
    private ArtifactRepositoryLayout layout;

    /**
     * @plexus.requirement
     */
    private AppareanceConfiguration appareanceConfiguration;

    public String execute()
        throws IOException, RegistryException
    {
        appConfiguration.save( configuration );

        return SUCCESS;
    }

    public String input()
        throws IOException, RegistryException
    {
        return INPUT;
    }

    public Object getModel()
    {
        return configuration;
    }

    public void prepare()
        throws ProjectBuildingException, ArtifactMetadataRetrievalException, SettingsConfigurationException,
        XmlPullParserException, IOException
    {

        Settings settings = mavenSettingsBuilder.buildSettings( false );

        // Load extra repositories from active profiles
        List<String> profileIds = settings.getActiveProfiles();
        List<Profile> profiles = settings.getProfiles();
        List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
        Map<String, Profile> profilesAsMap = settings.getProfilesAsMap();
        if ( profileIds != null && !profileIds.isEmpty() )
        {
            for ( String profileId : profileIds )
            {
                Profile profile = profilesAsMap.get( profileId );
                List<Repository> repos = profile.getRepositories();
                if ( repos != null && !repos.isEmpty() )
                {
                    for ( Repository repo : repos )
                    {
                        remoteRepositories.add( artifactRepositoryFactory.createArtifactRepository( repo.getId(), repo
                            .getUrl(), layout, null, null ) );
                    }
                }
            }
        }
        configuration = appConfiguration.getConfiguration();

        companyModel = companyPomHandler.getCompanyPomModel( configuration.getCompanyPom(),
                                                             helper.getLocalRepository(), remoteRepositories );

        this.setFooter( appareanceConfiguration.getFooter() );
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
