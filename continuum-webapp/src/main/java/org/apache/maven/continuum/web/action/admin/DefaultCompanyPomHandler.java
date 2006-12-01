package org.apache.maven.continuum.web.action.admin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.continuum.configuration.CompanyPom;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

/**
 * Holds a company POM from the repository, and is able to find the latest one in a repository.
 *
 * @plexus.component
 */
public class DefaultCompanyPomHandler
    implements CompanyPomHandler
{
    /**
     * The company POM.
     */
    private Model companyModel;

    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper helper;

    /**
     * @plexus.requirement
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * @plexus.requirement
     */
    private ArtifactInstaller installer;

    private static final String ORGANIZATION_LOGO_PROPERTY = "organization.logo";


    public Model getCompanyPomModel( CompanyPom companyPom )
        throws ProjectBuildingException, ArtifactMetadataRetrievalException
    {
        if ( companyPom != null )
        {
            if ( StringUtils.isNotEmpty( companyPom.getGroupId() ) &&
                StringUtils.isNotEmpty( companyPom.getArtifactId() ) )
            {
                if ( companyModel != null )
                {
                    if ( !companyPom.getGroupId().equals( companyModel.getGroupId() ) ||
                        !companyPom.getArtifactId().equals( companyModel.getArtifactId() ) )
                    {
                        companyModel = null;
                    }
                }

                if ( companyModel == null )
                {
                    Artifact artifact = artifactFactory.createProjectArtifact( companyPom.getGroupId(),
                                                                               companyPom.getArtifactId(),
                                                                               Artifact.RELEASE_VERSION );

                    ArtifactRepository localRepository = helper.getLocalRepository();

                    List repositories =
                        projectBuilder.buildStandaloneSuperProject( localRepository ).getRemoteArtifactRepositories();
                    List versions =
                        artifactMetadataSource.retrieveAvailableVersions( artifact, localRepository, repositories );

                    if ( !versions.isEmpty() )
                    {
                        Collections.sort( versions );

                        DefaultArtifactVersion artifactVersion =
                            (DefaultArtifactVersion) versions.get( versions.size() - 1 );
                        artifact = artifactFactory.createProjectArtifact( companyPom.getGroupId(),
                                                                          companyPom.getArtifactId(),
                                                                          artifactVersion.toString() );

                        MavenProject project =
                            projectBuilder.buildFromRepository( artifact, repositories, localRepository );

                        // We want the original model so that we don't get super POM pollution
                        companyModel = project.getOriginalModel();

                        // We need to manually propogate the values we want to appear for editing
                        if ( companyModel.getOrganization() == null )
                        {
                            companyModel.setOrganization( project.getOrganization() );
                        }
                        else
                        {
                            if ( companyModel.getOrganization().getName() == null )
                            {
                                companyModel.getOrganization().setName( project.getOrganization().getName() );
                            }
                            if ( companyModel.getOrganization().getUrl() == null )
                            {
                                companyModel.getOrganization().setUrl( project.getOrganization().getUrl() );
                            }
                        }
                        String logo = project.getProperties().getProperty( ORGANIZATION_LOGO_PROPERTY );
                        if ( logo != null )
                        {
                            companyModel.getProperties().setProperty( ORGANIZATION_LOGO_PROPERTY, logo );
                        }
                    }
                }
            }
        }
        return companyModel;
    }

    public void save( Model companyModel )
        throws IOException, ArtifactInstallationException
    {
        String v = companyModel.getVersion();
        String newVersion;
        if ( v != null )
        {
            DefaultArtifactVersion version = new DefaultArtifactVersion( v );

            newVersion = String.valueOf( version.getMajorVersion() + 1 );
        }
        else
        {
            newVersion = "1";
        }
        companyModel.setVersion( newVersion );

        Artifact artifact = artifactFactory.createProjectArtifact( companyModel.getGroupId(),
                                                                   companyModel.getArtifactId(), newVersion );

        ArtifactRepository localRepository = helper.getLocalRepository();
        File f = File.createTempFile( "maven", "pom" );
        f.deleteOnExit();

        FileWriter fileWriter = new FileWriter( f );
        try
        {
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write( fileWriter, companyModel );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }

        installer.install( f, artifact, localRepository );

        this.companyModel = companyModel;
    }
}
