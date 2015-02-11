package org.apache.maven.continuum.core.action;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.maven.m2.SettingsConfigurationException;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManagerException;
import org.apache.maven.continuum.utils.ContinuumUrlValidator;
import org.apache.maven.continuum.utils.URLUserInfo;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Resolve the project url being passed in and gather authentication information
 * if the url is so configured, then create the projects
 * Supports:
 * - standard maven-scm url
 * - MungedUrl https://username:password@host
 * - maven settings based, server = host and scm info set to username and password
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@Component( role = org.codehaus.plexus.action.Action.class, hint = "create-projects-from-metadata" )
public class CreateProjectsFromMetadataAction
    extends AbstractContinuumAction
{
    /**
     * Metadata url for adding projects.
     */
    private static final String KEY_URL = "url";

    private static final String KEY_PROJECT_BUILDER_ID = "builderId";

    private static final String KEY_PROJECT_BUILDING_RESULT = "projectBuildingResult";

    private static final String KEY_LOAD_RECURSIVE_PROJECTS = "loadRecursiveProjects";

    public static final String KEY_CHECKOUT_PROJECTS_IN_SINGLE_DIRECTORY = "checkoutProjectsInSingleDirectory";

    @Requirement
    private ContinuumProjectBuilderManager projectBuilderManager;

    @Requirement
    private MavenSettingsBuilder mavenSettingsBuilder;

    @Requirement( hint = "continuumUrl" )
    private ContinuumUrlValidator urlValidator;

    public void execute( Map context )
        throws ContinuumException, ContinuumProjectBuilderManagerException, ContinuumProjectBuilderException
    {
        String projectBuilderId = getProjectBuilderId( context );

        boolean loadRecursiveProjects = isLoadRecursiveProject( context );

        boolean checkoutProjectsInSingleDirectory = getBoolean( context, KEY_CHECKOUT_PROJECTS_IN_SINGLE_DIRECTORY );

        int projectGroupId = getProjectGroupId( context );

        String curl = getUrl( context );

        URL url;

        ContinuumProjectBuilder projectBuilder = projectBuilderManager.getProjectBuilder( projectBuilderId );

        ContinuumProjectBuildingResult result;

        try
        {
            BuildDefinitionTemplate buildDefinitionTemplate = getBuildDefinitionTemplate( context );
            if ( buildDefinitionTemplate == null )
            {
                buildDefinitionTemplate = projectBuilder.getDefaultBuildDefinitionTemplate();
            }
            if ( !curl.startsWith( "http" ) )
            {
                url = new URL( curl );

                result = projectBuilder.buildProjectsFromMetadata( url, null, null, loadRecursiveProjects,
                                                                   buildDefinitionTemplate,
                                                                   checkoutProjectsInSingleDirectory, projectGroupId );

            }
            else
            {
                url = new URL( curl );
                String username = null;
                String password = null;

                try
                {
                    Settings settings = getSettings();

                    getLogger().info( "checking for settings auth setup" );
                    if ( settings != null && settings.getServer( url.getHost() ) != null )
                    {
                        getLogger().info( "found setting based auth setup, using" );
                        Server server = settings.getServer( url.getHost() );

                        username = server.getUsername();
                        password = server.getPassword();
                    }
                }
                catch ( SettingsConfigurationException se )
                {
                    getLogger().warn( "problem with settings file, disabling scm resolution of username and password" );
                }

                if ( username == null )
                {
                    URLUserInfo urlUserInfo = urlValidator.extractURLUserInfo( curl );
                    username = urlUserInfo.getUsername();
                    password = urlUserInfo.getPassword();
                }

                if ( urlValidator.isValid( curl ) )
                {

                    result = projectBuilder.buildProjectsFromMetadata( url, username, password, loadRecursiveProjects,
                                                                       buildDefinitionTemplate,
                                                                       checkoutProjectsInSingleDirectory,
                                                                       projectGroupId );

                }
                else
                {
                    result = new ContinuumProjectBuildingResult();
                    getLogger().info( "Malformed URL (MungedHttpsURL is not valid): " + hidePasswordInUrl( curl ) );
                    result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
                }
            }

            if ( result.getProjects() != null )
            {
                String scmRootUrl = getScmRootUrl( result.getProjects() );

                if ( scmRootUrl == null || scmRootUrl.equals( "" ) )
                {
                    if ( curl.indexOf( "pom.xml" ) > 0 )
                    {
                        scmRootUrl = curl.substring( 0, curl.indexOf( "pom.xml" ) - 1 );
                    }
                    else
                    {
                        scmRootUrl = curl;
                    }
                }

                //setUrl( context, scmRootUrl );
                setProjectScmRootUrl( context, scmRootUrl );
            }
        }
        catch ( MalformedURLException e )
        {
            getLogger().info( "Malformed URL: " + hidePasswordInUrl( curl ), e );
            result = new ContinuumProjectBuildingResult();
            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }
        catch ( URISyntaxException e )
        {
            getLogger().info( "Malformed URL: " + hidePasswordInUrl( curl ), e );
            result = new ContinuumProjectBuildingResult();
            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }

        setProjectBuildingResult( context, result );
    }

    private String hidePasswordInUrl( String url )
    {
        int indexAt = url.indexOf( "@" );

        if ( indexAt < 0 )
        {
            return url;
        }

        String s = url.substring( 0, indexAt );

        int pos = s.lastIndexOf( ":" );

        return s.substring( 0, pos + 1 ) + "*****" + url.substring( indexAt );
    }

    private Settings getSettings()
        throws SettingsConfigurationException
    {
        try
        {
            return mavenSettingsBuilder.buildSettings();
        }
        catch ( IOException e )
        {
            throw new SettingsConfigurationException( "Error reading settings file", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new SettingsConfigurationException( e.getMessage(), e.getDetail(), e.getLineNumber(),
                                                      e.getColumnNumber() );
        }
    }

    private String getScmRootUrl( List<Project> projects )
    {
        String scmRootUrl = "";

        for ( Project project : projects )
        {
            String scmUrl = project.getScmUrl();

            scmRootUrl = getCommonPath( scmUrl, scmRootUrl );
        }

        return scmRootUrl;
    }

    private String getCommonPath( String path1, String path2 )
    {
        if ( path2 == null || path2.equals( "" ) )
        {
            return path1;
        }
        else
        {
            int indexDiff = StringUtils.differenceAt( path1, path2 );
            String commonPath = path1.substring( 0, indexDiff );

            if ( commonPath.lastIndexOf( '/' ) != commonPath.length() - 1 && !( path1.contains( new String(
                commonPath + "/" ) ) || path2.contains( new String( commonPath + "/" ) ) ) )
            {
                while ( commonPath.lastIndexOf( '/' ) != commonPath.length() - 1 )
                {
                    commonPath = commonPath.substring( 0, commonPath.length() - 1 );
                }
            }

            return commonPath;
        }
    }

    public ContinuumProjectBuilderManager getProjectBuilderManager()
    {
        return projectBuilderManager;
    }

    public void setProjectBuilderManager( ContinuumProjectBuilderManager projectBuilderManager )
    {
        this.projectBuilderManager = projectBuilderManager;
    }

    public MavenSettingsBuilder getMavenSettingsBuilder()
    {
        return mavenSettingsBuilder;
    }

    public void setMavenSettingsBuilder( MavenSettingsBuilder mavenSettingsBuilder )
    {
        this.mavenSettingsBuilder = mavenSettingsBuilder;
    }

    public ContinuumUrlValidator getUrlValidator()
    {
        return urlValidator;
    }

    public void setUrlValidator( ContinuumUrlValidator urlValidator )
    {
        this.urlValidator = urlValidator;
    }

    public static String getUrl( Map<String, Object> context )
    {
        return getString( context, KEY_URL );
    }

    public static void setUrl( Map<String, Object> context, String url )
    {
        context.put( KEY_URL, url );
    }

    public static String getProjectBuilderId( Map<String, Object> context )
    {
        return getString( context, KEY_PROJECT_BUILDER_ID );
    }

    public static void setProjectBuilderId( Map<String, Object> context, String projectBuilderId )
    {
        context.put( KEY_PROJECT_BUILDER_ID, projectBuilderId );
    }

    public static ContinuumProjectBuildingResult getProjectBuildingResult( Map<String, Object> context )
    {
        return (ContinuumProjectBuildingResult) getObject( context, KEY_PROJECT_BUILDING_RESULT );
    }

    private static void setProjectBuildingResult( Map<String, Object> context, ContinuumProjectBuildingResult result )
    {
        context.put( KEY_PROJECT_BUILDING_RESULT, result );
    }

    public static boolean isLoadRecursiveProject( Map<String, Object> context )
    {
        return getBoolean( context, KEY_LOAD_RECURSIVE_PROJECTS );
    }

    public static void setLoadRecursiveProject( Map<String, Object> context, boolean loadRecursiveProject )
    {
        context.put( KEY_LOAD_RECURSIVE_PROJECTS, loadRecursiveProject );
    }

    public static boolean isCheckoutProjectsInSingleDirectory( Map<String, Object> context )
    {
        return getBoolean( context, KEY_CHECKOUT_PROJECTS_IN_SINGLE_DIRECTORY );
    }

    public static void setCheckoutProjectsInSingleDirectory( Map<String, Object> context,
                                                             boolean checkoutProjectsInSingleDirectory )
    {
        context.put( KEY_CHECKOUT_PROJECTS_IN_SINGLE_DIRECTORY, checkoutProjectsInSingleDirectory );
    }
}
