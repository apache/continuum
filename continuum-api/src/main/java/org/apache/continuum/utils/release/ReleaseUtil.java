package org.apache.continuum.utils.release;

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

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReleaseUtil
{
    @SuppressWarnings( "unchecked" )
    public static Map<String, Object> getReleasePluginParameters( String workingDirectory, String pomFilename )
        throws Exception
    {
        Map<String, Object> params = new HashMap<String, Object>();

        // TODO: Use the model reader so we'll can get the plugin configuration from parent too
        Model model = getMavenModel( workingDirectory, pomFilename );

        if ( model.getBuild() != null && model.getBuild().getPlugins() != null )
        {
            for ( Plugin plugin : (List<Plugin>) model.getBuild().getPlugins() )
            {
                if ( plugin.getGroupId() != null && plugin.getGroupId().equals( "org.apache.maven.plugins" ) &&
                    plugin.getArtifactId() != null && plugin.getArtifactId().equals( "maven-release-plugin" ) )
                {
                    Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();

                    // TODO: use constants
                    if ( dom != null )
                    {
                        Xpp3Dom configuration = dom.getChild( "releaseLabel" );
                        if ( configuration != null )
                        {
                            params.put( "scm-tag", configuration.getValue() );
                        }

                        configuration = dom.getChild( "tag" );
                        if ( configuration != null )
                        {
                            params.put( "scm-tag", configuration.getValue() );
                        }

                        configuration = dom.getChild( "tagBase" );
                        if ( configuration != null )
                        {
                            params.put( "scm-tagbase", configuration.getValue() );
                        }

                        configuration = dom.getChild( "preparationGoals" );
                        if ( configuration != null )
                        {
                            params.put( "preparation-goals", configuration.getValue() );
                        }

                        configuration = dom.getChild( "arguments" );
                        if ( configuration != null )
                        {
                            params.put( "arguments", configuration.getValue() );
                        }

                        configuration = dom.getChild( "scmCommentPrefix" );
                        if ( configuration != null )
                        {
                            params.put( "scm-comment-prefix", configuration.getValue() );
                        }

                        configuration = dom.getChild( "autoVersionSubmodules" );
                        if ( configuration != null )
                        {
                            params.put( "auto-version-submodules", Boolean.valueOf( configuration.getValue() ) );
                        }

                        configuration = dom.getChild( "addSchema" );
                        if ( configuration != null )
                        {
                            params.put( "add-schema", Boolean.valueOf( configuration.getValue() ) );
                        }

                        configuration = dom.getChild( "useReleaseProfile" );
                        if ( configuration != null )
                        {
                            params.put( "use-release-profile", Boolean.valueOf( configuration.getValue() ) );
                        }

                        configuration = dom.getChild( "goals" );
                        if ( configuration != null )
                        {
                            String goals = configuration.getValue();
                            if ( model.getDistributionManagement() != null &&
                                model.getDistributionManagement().getSite() != null )
                            {
                                goals += " site-deploy";
                            }

                            params.put( "perform-goals", goals );
                        }
                    }
                }
            }
        }
        return params;
    }

    public static void processProject( String workingDirectory, String pomFilename, boolean autoVersionSubmodules,
                                       List<Map<String, String>> projects )
        throws Exception
    {
        Model model = getMavenModel( workingDirectory, pomFilename );

        if ( model.getGroupId() == null )
        {
            model.setGroupId( model.getParent().getGroupId() );
        }

        if ( model.getVersion() == null )
        {
            model.setVersion( model.getParent().getVersion() );
        }

        setProperties( model, autoVersionSubmodules, projects );

        for ( Iterator modules = model.getModules().iterator(); modules.hasNext(); )
        {
            String module = StringUtils.replace( modules.next().toString(), '\\', '/' );

            processProject( workingDirectory + "/" + module, "pom.xml", autoVersionSubmodules, projects );
        }
    }

    private static void setProperties( Model model, boolean autoVersionSubmodules, List<Map<String, String>> projects )
        throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put( "key", model.getGroupId() + ":" + model.getArtifactId() );

        if ( model.getName() == null )
        {
            model.setName( model.getArtifactId() );
        }
        params.put( "name", model.getName() );

        if ( !autoVersionSubmodules || projects.size() == 0 )
        {
            VersionInfo version = new DefaultVersionInfo( model.getVersion() );
            params.put( "release", version.getReleaseVersionString() );
            params.put( "dev", version.getNextVersion().getSnapshotVersionString() );
        }
        else
        {
            Map<String, String> rootParams = projects.get( 0 ); // get the root map
            params.put( "release", rootParams.get( "release" ) );
            params.put( "dev", rootParams.get( "dev" ) );
        }

        projects.add( params );
    }

    private static Model getMavenModel( String workingDirectory, String pomFilename )
        throws Exception
    {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        return pomReader.read( ReaderFactory.newXmlReader( new File( workingDirectory, pomFilename ) ) );
    }
}
