package org.apache.maven.continuum.installation;

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

import org.apache.maven.continuum.execution.ExecutorConfigurator;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.profile.ProfileService;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.installation.InstallationService"
 * TODO use some cache mechanism to prevent always reading from store ?
 * @since 13 juin 07
 */
public class DefaultInstallationService
    extends AbstractLogEnabled
    implements InstallationService, Initializable
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement role-hint="default"
     */
    private ProfileService profileService;

    private Map<String, ExecutorConfigurator> typesValues;

    // ---------------------------------------------
    // Plexus lifecycle
    // ---------------------------------------------

    public void initialize()
        throws InitializationException
    {
        this.typesValues = new HashMap<String, ExecutorConfigurator>();
        this.typesValues.put( InstallationService.ANT_TYPE,
                              new ExecutorConfigurator( "ant", "bin", "ANT_HOME", "-version" ) );

        this.typesValues.put( InstallationService.ENVVAR_TYPE, null );
        this.typesValues.put( InstallationService.JDK_TYPE,
                              new ExecutorConfigurator( "java", "bin", "JAVA_HOME", "-version" ) );
        this.typesValues.put( InstallationService.MAVEN1_TYPE,
                              new ExecutorConfigurator( "maven", "bin", "MAVEN_HOME", "-v" ) );
        this.typesValues
            .put( InstallationService.MAVEN2_TYPE, new ExecutorConfigurator( "mvn", "bin", "M2_HOME", "-v" ) );
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#add(org.apache.maven.continuum.model.system.Installation)
     */
    public Installation add( Installation installation )
        throws InstallationException
    {
        return this.add( installation, false );

    }

    public Installation add( Installation installation, boolean automaticProfile )
        throws InstallationException
    {

        // TODO must be done in the same transaction
        Installation storedOne = null;
        try
        {
            String envVarName = this.getEnvVar( installation.getType() );
            // override with the defined var name for defined types
            if ( StringUtils.isNotEmpty( envVarName ) )
            {
                installation.setVarName( envVarName );
            }
            storedOne = store.addInstallation( installation );
        }
        catch ( ContinuumStoreException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }
        try
        {
            if ( automaticProfile )
            {
                Profile profile = new Profile();
                profile.setName( storedOne.getName() );
                profile = profileService.addProfile( profile );
                profileService.addInstallationInProfile( profile, storedOne );
            }
        }
        catch ( ProfileException e )
        {
            throw new InstallationException( "failed to create automatic Profile " + e.getMessage(), e );
        }
        return storedOne;
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#delete(org.apache.maven.continuum.model.system.Installation)
     */
    public void delete( Installation installation )
        throws InstallationException
    {
        try
        {
            store.removeInstallation( installation );
        }
        catch ( ContinuumStoreException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }

    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#getAllInstallations()
     */
    @SuppressWarnings("unchecked")
    public List<Installation> getAllInstallations()
        throws InstallationException
    {
        try
        {
            List installations = store.getAllInstallations();
            return installations == null ? Collections.EMPTY_LIST : installations;
        }
        catch ( ContinuumStoreException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#getInstallation(int)
     */
    public Installation getInstallation( int installationId )
        throws InstallationException
    {
        try
        {
            return store.getInstallation( installationId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#update(org.apache.maven.continuum.model.system.Installation)
     */
    public void update( Installation installation )
        throws InstallationException
    {
        try
        {
            Installation stored = getInstallation( installation.getInstallationId() );
            if ( stored == null )
            {
                throw new InstallationException( "installation with name " + installation.getName() + " not exists" );
            }

            stored.setName( installation.getName() );
            stored.setType( installation.getType() );
            String envVarName = this.getEnvVar( installation.getType() );
            // override with the defined var name for defined types
            if ( StringUtils.isNotEmpty( envVarName ) )
            {
                installation.setVarName( envVarName );
            }
            else
            {
                stored.setVarName( installation.getVarName() );
            }
            stored.setVarValue( installation.getVarValue() );
            store.updateInstallation( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }

    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#getExecutorConfigurator(java.lang.String)
     */
    public ExecutorConfigurator getExecutorConfigurator( String type )
    {
        return this.typesValues.get( type );
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#getEnvVar(java.lang.String)
     */
    public String getEnvVar( String type )
    {
        ExecutorConfigurator executorConfigurator = this.typesValues.get( type );
        return executorConfigurator == null ? null : executorConfigurator.getEnvVar();
    }

    // -------------------------------------------------------------
    // versions informations on jdk and builders (mvn, maven, ant )
    // -------------------------------------------------------------

    /**
     * TODO replace with calling getExecutorConfiguratorVersion
     *
     * @see org.apache.maven.continuum.installation.InstallationService#getDefaultJdkInformations()
     */
    public List<String> getDefaultJdkInformations()
        throws InstallationException
    {
        try
        {
            Properties systemEnvVars = CommandLineUtils.getSystemEnvVars( false );

            String javaHome = (String) systemEnvVars.get( "JAVA_HOME" );
            // olamy : JAVA_HOME can not exists with a mac user
            if ( StringUtils.isEmpty( javaHome ) )
            {
                return getJavaHomeInformations( System.getProperty( "java.home" ) );
            }
            return getJavaHomeInformations( javaHome );

        }
        catch ( IOException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }
        catch ( CommandLineException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }
    }

    /**
     * TODO replace with calling getExecutorConfiguratorVersion
     *
     * @see org.apache.maven.continuum.installation.InstallationService#getJdkInformations(org.apache.maven.continuum.model.system.Installation)
     */
    public List<String> getJdkInformations( Installation installation )
        throws InstallationException
    {
        if ( installation == null )
        {
            return getDefaultJdkInformations();
        }
        if ( StringUtils.isEmpty( installation.getVarValue() ) )
        {
            return getDefaultJdkInformations();
        }
        try
        {
            return getJavaHomeInformations( installation.getVarValue() );
        }
        catch ( CommandLineException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }
    }

    /**
     * @param javaHome
     * @return
     * @throws CommandLineException
     */
    private List<String> getJavaHomeInformations( String javaHome )
        throws CommandLineException
    {
        Commandline commandline = new Commandline();

        String executable = javaHome + File.separator + "bin" + File.separator + "java";

        commandline.setExecutable( executable );
        commandline.addArguments( new String[]{"-version"} );
        final List<String> cliOutput = new ArrayList<String>();
        //TODO ShellCommandHelper ?
        int result = CommandLineUtils.executeCommandLine( commandline, new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                cliOutput.add( line );
            }
        }, new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                cliOutput.add( line );
            }
        } );
        if ( result != 0 )
        {
            throw new CommandLineException( "cli to get JAVA_HOME informations return code " + result );
        }
        return cliOutput;
    }

    private Map<String, String> getEnvVars( Profile profile )
    {
        Map<String, String> environnments = new HashMap<String, String>();
        if ( profile == null )
        {
            return environnments;
        }
        if ( profile.getBuilder() != null )
        {
            environnments.put( profile.getBuilder().getVarName(), profile.getBuilder().getVarValue() );
        }
        if ( profile.getJdk() != null )
        {
            environnments.put( profile.getJdk().getVarName(), profile.getJdk().getVarValue() );
        }
        if ( profile.getEnvironmentVariables() != null )
        {
            for ( Iterator iterator = profile.getEnvironmentVariables().iterator(); iterator.hasNext(); )
            {
                Installation installation = (Installation) iterator.next();
                environnments.put( installation.getVarName(), installation.getVarValue() );
            }
        }
        return environnments;
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#getExecutorConfiguratorVersion(java.lang.String,org.apache.maven.continuum.execution.ExecutorConfigurator,Profile)
     */
    @SuppressWarnings("unchecked")
    public List<String> getExecutorConfiguratorVersion( String path, ExecutorConfigurator executorConfigurator,
                                                        Profile profile )
        throws InstallationException
    {

        if ( executorConfigurator == null )
        {
            return Collections.EMPTY_LIST;
        }
        if ( executorConfigurator.getExecutable() == null )
        {
            return Collections.EMPTY_LIST;
        }
        StringBuilder executable = new StringBuilder();
        try
        {
            Commandline commandline = new Commandline();
            if ( StringUtils.isNotEmpty( path ) )
            {
                executable.append( path ).append( File.separator );
                executable.append( executorConfigurator.getRelativePath() + File.separator );
                commandline.addEnvironment( executorConfigurator.getEnvVar(), path );
            }
            //Installations are env var they must be add if exists
            Map<String, String> environments = getEnvVars( profile );
            // no null check we use a private method just here
            for ( Iterator<String> iterator = environments.keySet().iterator(); iterator.hasNext(); )
            {
                String key = iterator.next();
                String value = environments.get( key );
                commandline.addEnvironment( key, value );
            }

            executable = executable.append( executorConfigurator.getExecutable() );
            commandline.setExecutable( executable.toString() );
            commandline.addArguments( new String[]{executorConfigurator.getVersionArgument()} );
            final List<String> cliOutput = new ArrayList<String>();
            //TODO ShellCommandHelper ?
            int result = CommandLineUtils.executeCommandLine( commandline, new StreamConsumer()
            {
                public void consumeLine( String line )
                {
                    cliOutput.add( line );
                }
            }, new StreamConsumer()
            {
                public void consumeLine( String line )
                {
                    cliOutput.add( line );
                }
            } );
            if ( result != 0 )
            {
                throw new InstallationException( "cli to get " + executable + " version return code " + result );
            }
            return cliOutput;
        }
        catch ( CommandLineException e )
        {
            getLogger().error(
                "fail to execute " + executable + " with arg " + executorConfigurator.getVersionArgument() );
            throw new InstallationException( e.getMessage(), e );
        }
    }

}
