package org.apache.continuum.buildagent.build.execution.maven.m1;

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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component( role = org.apache.continuum.buildagent.build.execution.maven.m1.BuildAgentMavenOneMetadataHelper.class, hint = "default" )
public class DefaultBuildAgentMavenOneMetadataHelper
    implements BuildAgentMavenOneMetadataHelper
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentMavenOneMetadataHelper.class );

    public void mapMetadata( ContinuumProjectBuildingResult result, File metadata, Project project )
        throws BuildAgentMavenOneMetadataHelperException
    {
        Xpp3Dom mavenProject;

        try
        {
            mavenProject = Xpp3DomBuilder.build( new FileReader( metadata ) );
        }
        catch ( XmlPullParserException e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_XML_PARSE );

            log.info( "Error while reading maven POM (" + e.getMessage() + ").", e );

            return;
        }
        catch ( FileNotFoundException e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_POM_NOT_FOUND );

            log.info( "Error while reading maven POM (" + e.getMessage() + ").", e );

            return;
        }
        catch ( IOException e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );

            log.info( "Error while reading maven POM (" + e.getMessage() + ").", e );

            return;
        }

        // ----------------------------------------------------------------------
        // We cannot deal with projects that use the <extend/> element because
        // we don't have the whole source tree and we might be missing elements
        // that are present in the parent.
        // ----------------------------------------------------------------------

        String extend = getValue( mavenProject, "extend", null );

        if ( extend != null )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_EXTEND );

            log.info( "Cannot use a POM with an 'extend' element." );

            return;
        }

        // ----------------------------------------------------------------------
        // Artifact and group id
        // ----------------------------------------------------------------------

        String groupId;

        String artifactId;

        String id = getValue( mavenProject, "id", null );

        if ( !StringUtils.isEmpty( id ) )
        {
            groupId = id;

            artifactId = id;
        }
        else
        {
            groupId = getValue( mavenProject, "groupId", project.getGroupId() );

            if ( StringUtils.isEmpty( groupId ) )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_GROUPID );

                log.info( "Missing 'groupId' element in the POM." );

                // Do not throw an exception or return here, gather up as many results as possible first.
            }

            artifactId = getValue( mavenProject, "artifactId", project.getArtifactId() );

            if ( StringUtils.isEmpty( artifactId ) )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_ARTIFACTID );

                log.info( "Missing 'artifactId' element in the POM." );

                // Do not throw an exception or return here, gather up as many results as possible first.
            }
        }

        // ----------------------------------------------------------------------
        // version
        // ----------------------------------------------------------------------

        String version = getValue( mavenProject, "currentVersion", project.getVersion() );

        if ( StringUtils.isEmpty( project.getVersion() ) && StringUtils.isEmpty( version ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_VERSION );

            // Do not throw an exception or return here, gather up as many results as possible first.
        }

        // ----------------------------------------------------------------------
        // name
        // ----------------------------------------------------------------------

        String name = getValue( mavenProject, "name", project.getName() );

        if ( StringUtils.isEmpty( project.getName() ) && StringUtils.isEmpty( name ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_NAME );

            // Do not throw an exception or return here, gather up as many results as possible first.
        }

        // ----------------------------------------------------------------------
        // description
        // ----------------------------------------------------------------------

        String shortDescription = getValue( mavenProject, "shortDescription", project.getDescription() );

        String description = getValue( mavenProject, "description", project.getDescription() );

        // ----------------------------------------------------------------------
        // scm
        // ----------------------------------------------------------------------

        Xpp3Dom repository = mavenProject.getChild( "repository" );

        String scmConnection = null;

        if ( repository == null )
        {
            if ( !StringUtils.isEmpty( project.getScmUrl() ) )
            {
                scmConnection = project.getScmUrl();
            }
            else
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_REPOSITORY );

                // Do not throw an exception or return here, gather up as many results as possible first.
            }
        }
        else
        {
            scmConnection = getValue( repository, "developerConnection", project.getScmUrl() );

            scmConnection = getValue( repository, "connection", scmConnection );

            if ( StringUtils.isEmpty( scmConnection ) )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_SCM, name );

                // Do not throw an exception or return here, gather up as many results as possible first.
            }
        }

        // ----------------------------------------------------------------------
        // Developers
        // ----------------------------------------------------------------------

        Xpp3Dom developers = mavenProject.getChild( "developers" );

        if ( developers != null )
        {
            Xpp3Dom[] developersList = developers.getChildren();

            List<ProjectDeveloper> cds = new ArrayList<ProjectDeveloper>();

            for ( Xpp3Dom developer : developersList )
            {
                ProjectDeveloper cd = new ProjectDeveloper();

                cd.setScmId( getValue( developer, "id", null ) );

                cd.setName( getValue( developer, "name", null ) );

                cd.setEmail( getValue( developer, "email", null ) );

                cds.add( cd );
            }

            project.setDevelopers( cds );
        }

        // ----------------------------------------------------------------------
        // Dependencies
        // ----------------------------------------------------------------------

        Xpp3Dom dependencies = mavenProject.getChild( "dependencies" );

        if ( dependencies != null )
        {
            Xpp3Dom[] dependenciesList = dependencies.getChildren();

            List<ProjectDependency> deps = new ArrayList<ProjectDependency>();

            for ( Xpp3Dom dependency : dependenciesList )
            {
                ProjectDependency cd = new ProjectDependency();

                if ( getValue( dependency, "groupId", null ) != null )
                {
                    cd.setGroupId( getValue( dependency, "groupId", null ) );

                    cd.setArtifactId( getValue( dependency, "artifactId", null ) );
                }
                else
                {
                    cd.setGroupId( getValue( dependency, "id", null ) );

                    cd.setArtifactId( getValue( dependency, "id", null ) );
                }

                cd.setVersion( getValue( dependency, "version", null ) );

                deps.add( cd );
            }

            project.setDependencies( deps );
        }

        // ----------------------------------------------------------------------
        // notifiers
        // ----------------------------------------------------------------------

        Xpp3Dom build = mavenProject.getChild( "build" );

        List<ProjectNotifier> notifiers = new ArrayList<ProjectNotifier>();

        // Add project Notifier
        if ( build != null )
        {
            String nagEmailAddress = getValue( build, "nagEmailAddress", null );

            if ( nagEmailAddress != null )
            {
                Properties props = new Properties();

                props.put( "address", nagEmailAddress );

                ProjectNotifier notifier = new ProjectNotifier();

                notifier.setConfiguration( props );

                notifier.setFrom( ProjectNotifier.FROM_PROJECT );

                notifiers.add( notifier );
            }
        }

        // Add all user notifiers
        if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
        {
            for ( ProjectNotifier notif : (List<ProjectNotifier>) project.getNotifiers() )
            {
                if ( notif.isFromUser() )
                {
                    notifiers.add( notif );
                }
            }
        }

        // ----------------------------------------------------------------------
        // Handle Errors / Results
        // ----------------------------------------------------------------------

        if ( result.hasErrors() )
        {
            // prevent project creation if there are errors.
            return;
        }

        // ----------------------------------------------------------------------
        // Make the project
        // ----------------------------------------------------------------------

        project.setGroupId( groupId );

        project.setArtifactId( artifactId );

        project.setVersion( version );

        project.setName( name );

        if ( StringUtils.isEmpty( shortDescription ) )
        {
            project.setDescription( description );
        }
        else
        {
            project.setDescription( shortDescription );

        }

        project.setScmUrl( scmConnection );

        project.setNotifiers( notifiers );
    }

    //----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String getValue( Xpp3Dom dom, String key, String defaultValue )
    {
        Xpp3Dom child = dom.getChild( key );

        if ( child == null )
        {
            return defaultValue;
        }

        return child.getValue();
    }
}
