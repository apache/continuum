package org.apache.maven.continuum.execution.maven.m1;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.notification.ContinuumRecipientSource;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.MavenOneProject;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultMavenOneMetadataHelper
    extends AbstractLogEnabled
    implements MavenOneMetadataHelper
{
    // ----------------------------------------------------------------------
    // MavenOneMetadataHelper Implementation
    // ----------------------------------------------------------------------

    public void mapMetadata( File metadata, MavenOneProject project )
        throws MavenOneMetadataHelperException
    {
        Xpp3Dom mavenProject;

        try
        {
            mavenProject = Xpp3DomBuilder.build( new FileReader( metadata ) );
        }
        catch ( Exception e )
        {
            throw new MavenOneMetadataHelperException( "Error while reading maven POM.", e );
        }

        // ----------------------------------------------------------------------
        // Populating the descriptor
        // ----------------------------------------------------------------------

        // Name
        String name = getValue( mavenProject, "name", project.getName() );

        if ( StringUtils.isEmpty( name ) )
        {
            throw new MavenOneMetadataHelperException( "Missing <name> from the project descriptor." );
        }

        // Scm
        Xpp3Dom repository = mavenProject.getChild( "repository" );

        String scmConnection;

        if ( repository == null )
        {
            if ( !StringUtils.isEmpty( project.getScmUrl() ) )
            {
                scmConnection = project.getScmUrl();
            }
            else
            {
                throw new MavenOneMetadataHelperException( "The project descriptor is missing the SCM information." );
            }
        }
        else
        {
            scmConnection = getValue( repository, "developerConnection", project.getScmUrl() );

            scmConnection = getValue( repository, "connection", scmConnection );

            if ( StringUtils.isEmpty( scmConnection ) )
            {
                throw new MavenOneMetadataHelperException( "Missing both anonymous and developer scm connection urls." );
            }
        }

        // Nag email address
        Xpp3Dom build = mavenProject.getChild( "build" );

        List notifiers = null;

        ContinuumNotifier notifier = new ContinuumNotifier();

        if ( build == null )
        {
            if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
            {
                notifiers = project.getNotifiers();
            }
            else
            {
                throw new MavenOneMetadataHelperException( "Missing build section." );
            }
        }
        else
        {
            String currentNagEmailAddress = null;
            
            if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
            {
                for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
                {
                    ContinuumNotifier notif = (ContinuumNotifier) i.next();

                    // Can we have an other type for maven 1 project?
                    if ( "mail".equals( notif.getType() ) )
                    {
                        currentNagEmailAddress = notif.getConfiguration().getProperty( ContinuumRecipientSource.ADDRESS_FIELD );
                    }
                }
            }

            String nagEmailAddress = getValue( build, "nagEmailAddress", currentNagEmailAddress );

            if ( nagEmailAddress != null )
            {
                Properties props = new Properties();

                props.put( ContinuumRecipientSource.ADDRESS_FIELD, nagEmailAddress );

                notifier.setConfiguration( props );
            }

        }

        if ( notifiers == null && notifier.getConfiguration().isEmpty() )
        {
            throw new MavenOneMetadataHelperException( "Missing nag email address from the project descriptor." );
        }
        else
        {
            if ( notifiers == null )
            {
                notifiers = new ArrayList();
            }

            notifiers.add( notifier );
        }

        // Version
        String version = getValue( mavenProject, "currentVersion", project.getVersion() );

        if ( StringUtils.isEmpty( version ) )
        {
            throw new MavenOneMetadataHelperException( "Missing version from the project descriptor." );
        }

        // Goals
        if ( StringUtils.isEmpty( project.getGoals() ) )
        {
            project.setGoals( "clean:clean jar:install" );
        }

        // ----------------------------------------------------------------------
        // Make the project
        // ----------------------------------------------------------------------

        project.setName( name );

        project.setScmUrl( scmConnection );

        project.setNotifiers( notifiers );

        project.setVersion( version );
    }

    // ----------------------------------------------------------------------
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
