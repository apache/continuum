package org.apache.maven.continuum.web.action;

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

import org.apache.continuum.configuration.BuildAgentConfigurationException;
import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.web.action.AbstractReleaseAction;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.ReleaseListenerSummary;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Edwin Punzalan
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="releasePrepare"
 */
public class ReleasePrepareAction
    extends AbstractReleaseAction
{
    private static final String SCM_SVN_PROTOCOL_PREFIX = "scm:svn";

    private static final String SNAPSHOT_VERSION_SUFFIX = "-SNAPSHOT";

    private int projectId;

    private String releaseId;

    private String name;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private String scmTagBase;

    private String scmCommentPrefix;

    private boolean scmUseEditMode = false;

    private List<Map<String, String>> projects = new ArrayList<Map<String, String>>();

    private List<String> projectKeys;

    private List<String> devVersions;

    private List<String> relVersions;

    private String prepareGoals;

    private String arguments;

    private ReleaseResult result;

    private ContinuumReleaseManagerListener listener;

    private String projectGroupName = "";

    private List<Profile> profiles;

    private int profileId;

    private boolean autoVersionSubmodules = false;

    private boolean addSchema = true;

    private ReleaseListenerSummary listenerSummary;

    public String input()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        Project project = getContinuum().getProject( projectId );
        scmUsername = project.getScmUsername();
        scmPassword = project.getScmPassword();
        scmTag = project.getScmTag();

        if ( scmTag == null )
        {
            String version = project.getVersion();
            int idx = version.indexOf( SNAPSHOT_VERSION_SUFFIX );

            if ( idx >= 0 )
            {
                // strip the snapshot version suffix
                scmTag = project.getArtifactId() + "-" + version.substring( 0, idx );
            }
            else
            {
                scmTag = project.getArtifactId() + "-" + version;
            }
        }

        String scmUrl = project.getScmUrl();
        if ( scmUrl.startsWith( SCM_SVN_PROTOCOL_PREFIX ) )
        {
            scmTagBase = new SvnScmProviderRepository( scmUrl, scmUsername, scmPassword ).getTagBase();
            // strip the Maven scm protocol prefix
            scmTagBase = scmTagBase.substring( SCM_SVN_PROTOCOL_PREFIX.length() + 1 );
        }
        else
        {
            scmTagBase = "";
        }

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        //CONTINUUM-1503
        releaseManager.sanitizeTagName( scmUrl, scmTag );

        prepareGoals = "clean integration-test";

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager distributedReleaseManager = getContinuum().getDistributedReleaseManager();

            try
            {
                getReleasePluginParameters( distributedReleaseManager.getReleasePluginParameters( projectId, "pom.xml" ) );

                projects = distributedReleaseManager.processProject( projectId, "pom.xml", autoVersionSubmodules );
            }
            catch ( BuildAgentConfigurationException e )
            {
                List<String> args = new ArrayList<String>();
                args.add( e.getMessage() );

                addActionError( getText( "distributedBuild.releasePrepare.input.error", args ) );
                return ERROR;
            }
        }
        else
        {
            try
            {
                String workingDirectory = getContinuum().getWorkingDirectory( project.getId() ).getPath();
        
                getReleasePluginParameters( workingDirectory, "pom.xml" );
        
                processProject( workingDirectory, "pom.xml" );
            }
            catch ( Exception e )
            {   
                List<String> args = new ArrayList<String>();
                args.add( e.getMessage() );

                addActionError( getText( "releasePrepare.input.error", args ) );
                return ERROR;
            }
        }

        profiles = this.getContinuum().getProfileService().getAllProfiles();

        return SUCCESS;
    }

    private void getReleasePluginParameters( String workingDirectory, String pomFilename )
        throws Exception
    {
        //TODO: Use the model reader so we'll can get the plugin configuration from parent too
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read( new FileReader( new File( workingDirectory, pomFilename ) ) );

        if ( model.getBuild() != null && model.getBuild().getPlugins() != null )
        {
            for ( Plugin plugin : (List<Plugin>) model.getBuild().getPlugins() )
            {
                if ( plugin.getGroupId() != null && plugin.getGroupId().equals( "org.apache.maven.plugins" ) &&
                    plugin.getArtifactId() != null && plugin.getArtifactId().equals( "maven-release-plugin" ) )
                {
                    Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();

                    if ( dom != null )
                    {
                        Xpp3Dom configuration = dom.getChild( "releaseLabel" );
                        if ( configuration != null )
                        {
                            scmTag = configuration.getValue();
                        }

                        configuration = dom.getChild( "tag" );
                        if ( configuration != null )
                        {
                            scmTag = configuration.getValue();
                        }

                        configuration = dom.getChild( "tagBase" );
                        if ( configuration != null )
                        {
                            scmTagBase = configuration.getValue();
                        }

                        configuration = dom.getChild( "preparationGoals" );
                        if ( configuration != null )
                        {
                            prepareGoals = configuration.getValue();
                        }

                        configuration = dom.getChild( "arguments" );
                        if ( configuration != null )
                        {
                            arguments = configuration.getValue();
                        }

                        configuration = dom.getChild( "scmCommentPrefix" );
                        if ( configuration != null )
                        {
                            scmCommentPrefix = configuration.getValue();
                        }

                        configuration = dom.getChild( "autoVersionSubmodules" );
                        if ( configuration != null )
                        {
                            autoVersionSubmodules = Boolean.valueOf( configuration.getValue() );
                        }

                        configuration = dom.getChild( "addSchema" );
                        if ( configuration != null )
                        {
                            addSchema = Boolean.valueOf( configuration.getValue() );
                        }
                    }
                }
            }
        }
    }

    public String execute()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        Project project = getContinuum().getProject( projectId );
        
        name = project.getName();
        if ( name == null )
        {
            name = project.getArtifactId();
        }

        Profile profile = null;

        if ( profileId != -1 )
        {
            profile = getContinuum().getProfileService().getProfile( profileId );
        }

        Map<String, String> environments = getEnvironments( profile );

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager distributedReleaseManager = getContinuum().getDistributedReleaseManager();

            try
            {
                releaseId = distributedReleaseManager.releasePrepare( project, getReleaseProperties(), getRelVersionMap(), getDevVersionMap(), 
                                                                      environments );

                if ( releaseId == null )
                {
                    addActionError( "" );
                    return ERROR;
                }
            }
            catch ( BuildAgentConfigurationException e )
            {
                List<String> args = new ArrayList<String>();
                args.add( e.getMessage() );

                addActionError( getText( "distributedBuild.releasePrepare.release.error", args ) );
                return ERROR;
            }
        }
        else
        {
            listener = new DefaultReleaseManagerListener();

            String workingDirectory = getContinuum().getWorkingDirectory( projectId ).getPath();

            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

            String executable = getContinuum().getInstallationService()
                                .getExecutorConfigurator( InstallationService.MAVEN2_TYPE ).getExecutable();

            if ( environments != null )
            {
                String m2Home = environments.get( getContinuum().getInstallationService().getEnvVar( InstallationService.MAVEN2_TYPE ) );
                if ( StringUtils.isNotEmpty( m2Home ) )
                {
                    executable = m2Home + File.separator + "bin" + File.separator + executable;
                }
            }

            releaseId =
                releaseManager.prepare( project, getReleaseProperties(), getRelVersionMap(), getDevVersionMap(), listener,
                                        workingDirectory, environments, executable );
        }

        AuditLog event = new AuditLog( "Release id=" + releaseId, AuditLogConstants.PREPARE_RELEASE );
        event.setCategory( AuditLogConstants.PROJECT );
        event.setCurrentUser( getPrincipal() );
        event.log();

        return SUCCESS;
    }

    public String viewResult()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager distributedReleaseManager = getContinuum().getDistributedReleaseManager();

            try
            {
                result = distributedReleaseManager.getReleaseResult( releaseId );
            }
            catch ( BuildAgentConfigurationException e )
            {
                addActionError( "release" );
                return "viewResultError";
            }
        }
        else
        {
            result = (ReleaseResult) getContinuum().getReleaseManager().getReleaseResults().get( releaseId );
        }

        return "viewResult";
    }

    public String checkProgress()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        String status;

        listenerSummary = new ReleaseListenerSummary();

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager distributedReleaseManager = getContinuum().getDistributedReleaseManager();
            Map listenerMap;
            try
            {
                listenerMap  = distributedReleaseManager.getListener( releaseId );
            }
            catch ( BuildAgentConfigurationException e )
            {
                addActionError( "" );
                return "";
            }
                
            if ( listenerMap != null && !listenerMap.isEmpty() )
            {
                int state = DistributedReleaseUtil.getReleaseState( listenerMap );

                if ( state == ContinuumReleaseManagerListener.FINISHED )
                {
                    distributedReleaseManager.removeListener( releaseId );
    
                    result = distributedReleaseManager.getReleaseResult( releaseId );
    
                    status = "finished";
                }
                else
                {
                    status = "inProgress";
                }

                listenerSummary.setPhases( DistributedReleaseUtil.getReleasePhases( listenerMap ) );
                listenerSummary.setCompletedPhases( DistributedReleaseUtil.getCompletedReleasePhases( listenerMap ) );
                listenerSummary.setInProgress( DistributedReleaseUtil.getReleaseInProgress( listenerMap ) );
                listenerSummary.setError( DistributedReleaseUtil.getReleaseError( listenerMap ) );
            }
            else
            {
                throw new Exception( "There is no release on-going or finished with id: " + releaseId );
            }
        }
        else
        {
            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();
    
            listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get( releaseId );
    
            if ( listener != null )
            {
                if ( listener.getState() == ContinuumReleaseManagerListener.FINISHED )
                {
                    releaseManager.getListeners().remove( releaseId );
    
                    result = (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );
    
                    status = "finished";
                }
                else
                {
                    status = "inProgress";
                }

                listenerSummary.setPhases( listener.getPhases() );
                listenerSummary.setCompletedPhases( listener.getCompletedPhases() );
                listenerSummary.setInProgress( listener.getInProgress() );
                listenerSummary.setError( listener.getError() );
            }
            else
            {
                throw new Exception( "There is no release on-going or finished with id: " + releaseId );
            }
        }

        return status;
    }

    private void processProject( String workingDirectory, String pomFilename )
        throws Exception
    {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read( new FileReader( new File( workingDirectory, pomFilename ) ) );

        if ( model.getGroupId() == null )
        {
            model.setGroupId( model.getParent().getGroupId() );
        }

        if ( model.getVersion() == null )
        {
            model.setVersion( model.getParent().getVersion() );
        }

        setProperties( model );

        if ( !autoVersionSubmodules )
        {
            for ( Iterator modules = model.getModules().iterator(); modules.hasNext(); )
            {
                processProject( workingDirectory + "/" + modules.next().toString(), "pom.xml" );
            }
        }
    }

    private void setProperties( Model model )
        throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put( "key", model.getGroupId() + ":" + model.getArtifactId() );

        if ( model.getName() == null )
        {
            model.setName( model.getArtifactId() );
        }
        params.put( "name", model.getName() );

        VersionInfo version = new DefaultVersionInfo( model.getVersion() );

        params.put( "release", version.getReleaseVersionString() );
        params.put( "dev", version.getNextVersion().getSnapshotVersionString() );

        projects.add( params );
    }

    private Map<String, String> getDevVersionMap()
    {
        return getVersionMap( projectKeys, devVersions );
    }

    private Map<String, String> getRelVersionMap()
    {
        return getVersionMap( projectKeys, relVersions );
    }

    private Map<String, String> getVersionMap( List<String> keys, List<String> versions )
    {
        Map<String, String> versionMap = new HashMap<String, String>();

        for ( int idx = 0; idx < keys.size(); idx++ )
        {
            String key = keys.get( idx );
            String version;
            if ( !autoVersionSubmodules )
            {
                version = versions.get( idx );
            }
            else
            {
                version = versions.get( 0 );
            }

            versionMap.put( key, version );
        }

        return versionMap;
    }

    private Properties getReleaseProperties()
    {
        Properties p = new Properties();

        if ( StringUtils.isNotEmpty( scmUsername ) )
        {
            p.setProperty( "username", scmUsername );
        }

        if ( StringUtils.isNotEmpty( scmPassword ) )
        {
            p.setProperty( "password", scmPassword );
        }

        if ( StringUtils.isNotEmpty( scmTagBase ) )
        {
            p.setProperty( "tagBase", scmTagBase );
        }

        if ( StringUtils.isNotEmpty( scmCommentPrefix ) )
        {
            p.setProperty( "commentPrefix", scmCommentPrefix );
        }

        p.setProperty( "tag", scmTag );
        p.setProperty( "prepareGoals", prepareGoals );
        p.setProperty( "arguments", arguments );
        p.setProperty( "useEditMode", Boolean.toString( scmUseEditMode ) );
        p.setProperty( "addSchema", Boolean.toString( addSchema ) );
        p.setProperty( "autoVersionSubmodules", Boolean.toString( autoVersionSubmodules ) );

        return p;
    }

    private void getReleasePluginParameters( Map context )
    {
        scmTag = DistributedReleaseUtil.getScmTag( context, scmTag );

        scmTagBase = DistributedReleaseUtil.getScmTagBase( context, scmTagBase );

        prepareGoals = DistributedReleaseUtil.getPrepareGoals( context, prepareGoals );

        arguments = DistributedReleaseUtil.getArguments( context, "" );

        scmCommentPrefix = DistributedReleaseUtil.getScmCommentPrefix( context, "" );

        autoVersionSubmodules = DistributedReleaseUtil.getAutoVersionSubmodules( context, false );

        addSchema = DistributedReleaseUtil.getAddSchema( context, true );
    }

    public List<String> getProjectKeys()
    {
        return projectKeys;
    }

    public void setProjectKeys( List<String> projectKeys )
    {
        this.projectKeys = projectKeys;
    }

    public List<String> getDevVersions()
    {
        return devVersions;
    }

    public void setDevVersions( List<String> devVersions )
    {
        this.devVersions = devVersions;
    }

    public List<String> getRelVersions()
    {
        return relVersions;
    }

    public void setRelVersions( List<String> relVersions )
    {
        this.relVersions = relVersions;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public String getScmTag()
    {
        return scmTag;
    }

    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    public String getScmTagBase()
    {
        return scmTagBase;
    }

    public void setScmTagBase( String scmTagBase )
    {
        this.scmTagBase = scmTagBase;
    }

    public List<Map<String, String>> getProjects()
    {
        return projects;
    }

    public void setProjects( List<Map<String, String>> projects )
    {
        this.projects = projects;
    }

    public ContinuumReleaseManagerListener getListener()
    {
        return listener;
    }

    public void setListener( DefaultReleaseManagerListener listener )
    {
        this.listener = listener;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }

    public String getPrepareGoals()
    {
        return prepareGoals;
    }

    public void setPrepareGoals( String prepareGoals )
    {
        this.prepareGoals = prepareGoals;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments( String arguments )
    {
        this.arguments = arguments;
    }

    public void validate()
    {
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }

    public List<Profile> getProfiles()
    {
        return profiles;
    }

    public void setProfiles( List<Profile> profiles )
    {
        this.profiles = profiles;
    }

    public int getProfileId()
    {
        return profileId;
    }

    public void setProfileId( int profileId )
    {
        this.profileId = profileId;
    }

    public boolean isScmUseEditMode()
    {
        return scmUseEditMode;
    }

    public void setScmUseEditMode( boolean scmUseEditMode )
    {
        this.scmUseEditMode = scmUseEditMode;
    }

    public String getScmCommentPrefix()
    {
        return scmCommentPrefix;
    }

    public void setScmCommentPrefix( String scmCommentPrefix )
    {
        this.scmCommentPrefix = scmCommentPrefix;
    }

    public boolean isAutoVersionSubmodules()
    {
        return autoVersionSubmodules;
    }

    public void setAutoVersionSubmodules( boolean autoVersionSubmodules )
    {
        this.autoVersionSubmodules = autoVersionSubmodules;
    }

    public boolean isAddSchema()
    {
        return addSchema;
    }

    public void setAddSchema( boolean addSchema )
    {
        this.addSchema = addSchema;
    }

    public ReleaseListenerSummary getListenerSummary()
    {
        return listenerSummary;
    }

    public void setListenerSummary( ReleaseListenerSummary listenerSummary )
    {
        this.listenerSummary = listenerSummary;
    }
}
