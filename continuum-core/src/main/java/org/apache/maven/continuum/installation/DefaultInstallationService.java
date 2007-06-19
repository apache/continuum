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

import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
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
    implements InstallationService, Initializable
{

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    private Map<String, String> typesValues;

    // ---------------------------------------------
    // Plexus lifecycle
    // ---------------------------------------------

    public void initialize()
        throws InitializationException
    {
        // TODO move this in a component configuration 
        this.typesValues = new HashMap<String, String>();
        this.typesValues.put( InstallationService.ANT_TYPE, "ANT_HOME" );
        this.typesValues.put( InstallationService.ENVVAR_TYPE, null );
        this.typesValues.put( InstallationService.JDK_TYPE, "JAVA_HOME" );
        this.typesValues.put( InstallationService.MAVEN1_TYPE, "MAVEN_HOME" );
        this.typesValues.put( InstallationService.MAVEN2_TYPE, "M2_HOME" );
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#add(org.apache.maven.continuum.model.system.Installation)
     */
    public Installation add( Installation installation )
        throws InstallationException
    {
        try
        {
            String envVarName = this.typesValues.get( installation.getType() );
            // override with the defined var name for defined types
            if ( StringUtils.isNotEmpty( envVarName ) )
            {
                installation.setVarName( envVarName );
            }
            return store.addInstallation( installation );
        }
        catch ( ContinuumStoreException e )
        {
            throw new InstallationException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#delete(org.apache.maven.continuum.model.system.Installation)
     */
    public void delete( Installation installation )
        throws InstallationException
    {
        // TODO remove the installations attached to profiles : jdo failed
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
     * @see org.apache.maven.continuum.installation.InstallationService#getInstallation(java.lang.String)
     */
    public Installation getInstallation( String name )
        throws InstallationException
    {
        try
        {
            return store.getInstallationByName( name );
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
            Installation stored = getInstallation( installation.getName() );
            if ( stored == null )
            {
                throw new InstallationException( "installation with name " + installation.getName() + " not exists" );
            }

            stored.setName( installation.getName() );
            stored.setType( installation.getType() );
            String envVarName = this.typesValues.get( installation.getType() );
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
     * @see org.apache.maven.continuum.installation.InstallationService#getEnvVar(java.lang.String)
     */
    public String getEnvVar( String type )
    {
        return (String) this.typesValues.get( type );
    }

    // -------------------------------------------------------------
    // versions informations on jdk and builders (mvn, maven, ant )
    // -------------------------------------------------------------

    /**
     * @see org.apache.maven.continuum.installation.InstallationService#getDefaultJdkInformations()
     */
    public List<String> getDefaultJdkInformations()
        throws InstallationException
    {
        try
        {
            Properties systemEnvVars = CommandLineUtils.getSystemEnvVars( false );

            String javaHome = (String) systemEnvVars.get( "JAVA_HOME" );
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
     * @see org.apache.maven.continuum.installation.InstallationService#getJdkInformations(org.apache.maven.continuum.model.system.Installation)
     */
    public List<String> getJdkInformations( Installation installation )
        throws InstallationException
    {
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

    private List<String> getJavaHomeInformations( String javaHome )
        throws CommandLineException
    {
        Commandline commandline = new Commandline();

        String executable = javaHome + File.separator + "bin" + File.separator + "java";
        /*
        if ( Os.isFamily( Os.FAMILY_DOS ) )
        {
            executable = "%JAVA_HOME%\\bin\\java";
        }
        else
        {
            executable = "$JAVA_HOME/bin/java";
        }
        */
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
}
