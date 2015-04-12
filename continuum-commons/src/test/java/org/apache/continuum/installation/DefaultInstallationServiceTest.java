package org.apache.continuum.installation;

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

import org.apache.continuum.dao.DaoUtils;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.execution.ExecutorConfigurator;
import org.apache.maven.continuum.installation.AlreadyExistsInstallationException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.ProfileService;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @since 13 juin 07
 */
public class DefaultInstallationServiceTest
    extends AbstractContinuumTest
{
    private static final String NEW_INSTALLATION_NAME = "newInstallation";

    @Before
    public void setUp()
        throws Exception
    {
        DaoUtils daoUtils = lookup( DaoUtils.class );
        daoUtils.eraseDatabase();
    }

    private InstallationService getInstallationService()
        throws Exception
    {
        //Continuum continuum = (Continuum) lookup( Continuum.ROLE );
        //return continuum.getInstallationService();
        return lookup( InstallationService.class );
    }

    private Installation addInstallation( String name, String varName, String varValue, String type )
        throws Exception
    {

        Installation installation = new Installation();
        installation.setType( type );
        installation.setName( name );
        installation.setVarName( varName );
        installation.setVarValue( varValue );
        return getInstallationService().add( installation );
    }

    @Test
    public void testAddInstallation()
        throws Exception
    {
        Installation added = this.addInstallation( NEW_INSTALLATION_NAME, null, "bar", InstallationService.JDK_TYPE );
        Installation getted = getInstallationService().getInstallation( added.getInstallationId() );
        assertNotNull( getted );
        assertEquals( getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), getted.getVarName() );
        assertEquals( "bar", getted.getVarValue() );
        assertEquals( 1, getInstallationService().getAllInstallations().size() );
        assertNotNull( getInstallationService().getInstallation( NEW_INSTALLATION_NAME ) );
    }

    @Test
    public void testAddDuplicateInstallation()
        throws Exception
    {
        Installation added = this.addInstallation( NEW_INSTALLATION_NAME, null, "bar", InstallationService.JDK_TYPE );
        Installation getted = getInstallationService().getInstallation( added.getInstallationId() );
        assertNotNull( getted );
        assertEquals( getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), getted.getVarName() );
        assertEquals( "bar", getted.getVarValue() );
        try
        {
            this.addInstallation( NEW_INSTALLATION_NAME, null, "bar", InstallationService.JDK_TYPE );
            fail( "not in AlreadyExistsInstallationException" );
        }
        catch ( AlreadyExistsInstallationException e )
        {
            // we must be here
        }
        assertEquals( 1, getInstallationService().getAllInstallations().size() );
    }

    @Test
    public void testRemove()
        throws Exception
    {
        String name = "toremove";
        Installation added = this.addInstallation( name, "foo", "bar", InstallationService.JDK_TYPE );
        Installation getted = getInstallationService().getInstallation( added.getInstallationId() );
        assertNotNull( getted );
        getInstallationService().delete( getted );
        getted = getInstallationService().getInstallation( added.getInstallationId() );
        assertNull( getted );

    }

    @Test
    public void testUpdate()
        throws Exception
    {
        String name = "toupdate";
        Installation added = this.addInstallation( name, "foo", "bar", InstallationService.JDK_TYPE );
        Installation getted = getInstallationService().getInstallation( added.getInstallationId() );
        assertNotNull( getted );
        assertEquals( getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), getted.getVarName() );
        assertEquals( "bar", getted.getVarValue() );
        getted.setVarName( "updatefoo" );
        getted.setVarValue( "updatedbar" );
        getInstallationService().update( getted );
        getted = getInstallationService().getInstallation( added.getInstallationId() );
        assertNotNull( getted );
        assertEquals( getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), getted.getVarName() );
        assertEquals( "updatedbar", getted.getVarValue() );
    }

    @Test
    public void testGetDefaultJavaVersionInfo()
        throws Exception
    {
        InstallationService installationService = (InstallationService) lookup( InstallationService.ROLE, "default" );
        List<String> infos = installationService.getDefaultJavaVersionInfo();
        assertNotNull( infos );
    }

    @Test
    public void testGetJavaVersionInfo()
        throws Exception
    {
        InstallationService installationService = (InstallationService) lookup( InstallationService.ROLE, "default" );
        String javaHome = System.getenv( "JAVA_HOME" );
        if ( StringUtils.isEmpty( javaHome ) )
        {
            javaHome = System.getProperty( "java.home" );
        }
        Installation installation = new Installation();
        installation.setName( "test" );
        installation.setType( InstallationService.JDK_TYPE );
        installation.setVarValue( javaHome );

        List<String> infos = installationService.getJavaVersionInfo( installation );
        assertNotNull( infos );
    }

    @Test
    public void testGetJavaVersionInfoWithCommonMethod()
        throws Exception
    {
        InstallationService installationService = (InstallationService) lookup( InstallationService.ROLE, "default" );
        ExecutorConfigurator java = installationService.getExecutorConfigurator( InstallationService.JDK_TYPE );
        String javaHome = System.getenv( "JAVA_HOME" );
        if ( StringUtils.isEmpty( javaHome ) )
        {
            javaHome = System.getProperty( "java.home" );
        }
        List<String> infos = installationService.getExecutorVersionInfo( javaHome, java, null );
        System.out.println( infos );
        assertNotNull( infos );
    }

/* CONTINUUM-2559 - test may fail even in a valid environment
    public void testgetMvnVersionWithCommonMethod()
        throws Exception
    {
        InstallationService installationService = (InstallationService) lookup( InstallationService.ROLE, "default" );
        ExecutorConfigurator java = installationService.getExecutorConfigurator( InstallationService.MAVEN2_TYPE );
        List<String> infos = installationService.getExecutorVersionInfo( null, java, null );
        assertNotNull( infos );
    }
*/

    @Test
    public void testAddInstallationAutomaticProfile()
        throws Exception
    {
        Installation installation = new Installation();
        installation.setType( InstallationService.JDK_TYPE );
        installation.setName( "automaticJdk" );
        installation.setVarName( "automaticvarName" );
        installation.setVarValue( "automaticvarValue" );
        getInstallationService().add( installation, true );
        ProfileService profileService = (ProfileService) lookup( ProfileService.ROLE, "default" );
        List<Profile> profiles = profileService.getAllProfiles();
        assertEquals( 1, profiles.size() );
        Profile profile = profiles.get( 0 );
        assertEquals( "automaticJdk", profile.getName() );
        Installation jdk = profile.getJdk();
        assertNotNull( jdk );
        assertEquals( "automaticJdk", jdk.getName() );
    }

    @Test
    public void testUpdateName()
        throws Exception
    {
        Installation installation = new Installation();
        installation.setType( InstallationService.JDK_TYPE );
        installation.setName( "automatic" );
        installation.setVarName( "automaticvarName" );
        installation.setVarValue( "automaticvarValue" );
        installation = getInstallationService().add( installation, true );

        installation.setName( "new name here" );
        getInstallationService().update( installation );

        Installation getted = getInstallationService().getInstallation( installation.getInstallationId() );
        assertEquals( "new name here", getted.getName() );

    }
}
