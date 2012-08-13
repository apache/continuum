package org.apache.continuum.profile;

import org.apache.continuum.dao.DaoUtils;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.AlreadyExistsProfileException;
import org.apache.maven.continuum.profile.ProfileService;

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
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @since 15 juin 07
 */
public class DefaultProfileServiceTest
    extends AbstractContinuumTest
{

    Installation jdk1;

    private static final String jdk1Name = "jdk1";

    private Installation jdk2;

    private static final String jdk2Name = "jdk2";

    Installation mvn205;

    private static final String mvn205Name = "mvn 2.0.5";

    Installation mvn206;

    private static final String mvn206Name = "mvn 2.0.6";

    Profile jdk1mvn205;

    private static final String jdk1mvn205Name = "jdk1 mvn 2.0.5";

    Profile jdk2mvn206;

    private static final String jdk2mvn206Name = "jdk2 mvn 2.0.6";

    Installation mvnOpts1;

    private static final String mvnOpts1Name = "mvnOpts1";

    Installation mvnOpts2;

    private static final String mvnOpts2Name = "mvnOpts2";

    protected void setUp()
        throws Exception
    {
        super.setUp();
        DaoUtils daoUtils = (DaoUtils) lookup( DaoUtils.class.getName() );
        daoUtils.eraseDatabase();

        jdk1 = new Installation();
        jdk1.setType( InstallationService.JDK_TYPE );
        jdk1.setVarValue( "/foo/bar" );
        jdk1.setName( jdk1Name );
        jdk1 = getInstallationService().add( jdk1 );

        jdk2 = new Installation();
        jdk2.setType( InstallationService.JDK_TYPE );
        jdk2.setVarValue( "/foo/bar/zloug" );
        jdk2.setName( jdk2Name );
        jdk2 = getInstallationService().add( jdk2 );

        mvn205 = new Installation();
        mvn205.setType( InstallationService.MAVEN2_TYPE );
        mvn205.setVarValue( "/users/maven-2.0.5" );
        mvn205.setName( mvn205Name );
        mvn205 = getInstallationService().add( mvn205 );

        mvn206 = new Installation();
        mvn206.setType( InstallationService.MAVEN2_TYPE );
        mvn206.setVarValue( "/users/maven-2.0.6" );
        mvn206.setName( mvn206Name );
        mvn206 = getInstallationService().add( mvn206 );

        jdk1mvn205 = new Profile();
        jdk1mvn205.setJdk( jdk1 );
        jdk1mvn205.setBuilder( mvn205 );
        jdk1mvn205.setName( jdk1mvn205Name );
        getProfileService().addProfile( jdk1mvn205 );

        jdk2mvn206 = new Profile();
        jdk2mvn206.setJdk( jdk2 );
        jdk2mvn206.setBuilder( mvn206 );
        jdk2mvn206.setName( jdk2mvn206Name );
        getProfileService().addProfile( jdk2mvn206 );

        mvnOpts1 = new Installation();
        mvnOpts1.setType( InstallationService.ENVVAR_TYPE );
        mvnOpts1.setVarName( "MAVEN_OPTS" );
        mvnOpts1.setVarValue( "-Xmx256m -Djava.awt.headless=true" );
        mvnOpts1.setName( mvnOpts1Name );
        mvnOpts1 = getInstallationService().add( mvnOpts1 );

        mvnOpts2 = new Installation();
        mvnOpts2.setType( InstallationService.ENVVAR_TYPE );
        mvnOpts2.setVarName( "MAVEN_OPTS" );
        mvnOpts2.setVarValue( "-Xmx1024m -Xms1024m" );
        mvnOpts2.setName( mvnOpts2Name );
        mvnOpts2 = getInstallationService().add( mvnOpts2 );

    }

    public InstallationService getInstallationService()
        throws Exception
    {
        return (InstallationService) lookup( InstallationService.ROLE, "default" );
    }

    public ProfileService getProfileService()
        throws Exception
    {
        return (ProfileService) lookup( ProfileService.ROLE, "default" );
    }

    public void testAddProfile()
        throws Exception
    {
        Profile defaultProfile = new Profile();
        String name = "default profile";
        defaultProfile.setName( name );
        Profile getted = getProfileService().addProfile( defaultProfile );
        assertNotNull( getProfileService().getProfile( getted.getId() ) );
        assertEquals( name, getProfileService().getProfile( getted.getId() ).getName() );
        assertEquals( 3, getProfileService().getAllProfiles().size() );
    }

    public void testAddDuplicateProfile()
        throws Exception
    {
        Profile defaultProfile = new Profile();
        String name = "default profile";
        defaultProfile.setName( name );
        Profile getted = getProfileService().addProfile( defaultProfile );
        assertNotNull( getProfileService().getProfile( getted.getId() ) );
        assertEquals( name, getProfileService().getProfile( getted.getId() ).getName() );
        assertEquals( 3, getProfileService().getAllProfiles().size() );

        defaultProfile = new Profile();
        defaultProfile.setName( name );
        try
        {
            getProfileService().addProfile( defaultProfile );
            fail( "no AlreadyExistsProfileException with an already exist name " );
        }
        catch ( AlreadyExistsProfileException e )
        {
            // we must be here
        }
        assertEquals( 3, getProfileService().getAllProfiles().size() );
    }

    public void testDeleteProfile()
        throws Exception
    {
        Profile defaultProfile = new Profile();
        defaultProfile.setName( "default profile" );
        Profile getted = getProfileService().addProfile( defaultProfile );
        int id = getted.getId();
        assertNotNull( getProfileService().getProfile( id ) );
        getProfileService().deleteProfile( id );
        assertNull( getProfileService().getProfile( id ) );
    }

    public void testgetAllProfile()
        throws Exception
    {
        List<Profile> all = getProfileService().getAllProfiles();
        assertNotNull( all );
        assertFalse( all.isEmpty() );
        assertEquals( 2, all.size() );
    }

    public void testupdateProfile()
        throws Exception
    {
        Profile profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertEquals( jdk1mvn205Name, profile.getName() );
        String newName = "new name";
        profile.setName( newName );
        getProfileService().updateProfile( profile );

        Profile getted = getProfileService().getProfile( jdk1mvn205.getId() );
        assertNotNull( getted );
        assertEquals( newName, getted.getName() );
    }

    public void testupdateProfileDuplicateName()
        throws Exception
    {
        int profileId = jdk1mvn205.getId();
        Profile profile = getProfileService().getProfile( profileId );
        assertEquals( jdk1mvn205Name, profile.getName() );
        profile.setName( jdk2mvn206Name );

        try
        {
            getProfileService().updateProfile( profile );

            fail( "no AlreadyExistsProfileException with duplicate name" );
        }
        catch ( AlreadyExistsProfileException e )
        {
            // we must be here
        }
        Profile getted = getProfileService().getProfile( profileId );
        assertNotNull( getted );
        assertEquals( jdk1mvn205Name, getted.getName() );
    }

    public void testsetJdkInProfile()
        throws Exception
    {
        Profile profile = getProfileService().getProfile( jdk1mvn205.getId() );
        getProfileService().setJdkInProfile( profile, jdk2 );

        profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertEquals( jdk2.getName(), profile.getJdk().getName() );
        assertEquals( jdk2.getVarValue(), profile.getJdk().getVarValue() );
    }

    public void testsetBuilderInProfile()
        throws Exception
    {
        Profile profile = getProfileService().getProfile( jdk1mvn205.getId() );
        getProfileService().setBuilderInProfile( profile, mvn206 );
        profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertEquals( mvn206.getName(), profile.getBuilder().getName() );
        assertEquals( mvn206.getVarValue(), profile.getBuilder().getVarValue() );

    }

    public void testaddEnvVarInProfile()
        throws Exception
    {
        Profile profile = getProfileService().getProfile( jdk1mvn205.getId() );
        getProfileService().setBuilderInProfile( profile, mvn206 );
        getProfileService().addEnvVarInProfile( profile, mvnOpts1 );
        profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertFalse( profile.getEnvironmentVariables().isEmpty() );
        assertEquals( 1, profile.getEnvironmentVariables().size() );
    }

    public void testRemoveInstallationLinkedToAProfile()
        throws Exception
    {
        Profile profile = getProfileService().getProfile( jdk1mvn205.getId() );
        getProfileService().setJdkInProfile( profile, jdk2 );

        getProfileService().getProfile( jdk1mvn205.getId() );
        InstallationService installationService = (InstallationService) lookup( InstallationService.ROLE, "default" );
        installationService.delete( jdk2 );
    }

    public void testRemoveEnvVarFromProfile()
        throws Exception
    {
        Profile profile = getProfileService().getProfile( jdk1mvn205.getId() );
        getProfileService().setJdkInProfile( profile, jdk2 );
        getProfileService().addEnvVarInProfile( profile, mvnOpts1 );
        getProfileService().addEnvVarInProfile( profile, mvnOpts2 );

        profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertNotNull( profile.getJdk() );
        assertEquals( 2, profile.getEnvironmentVariables().size() );

        getProfileService().removeInstallationFromProfile( profile, mvnOpts1 );

        profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertNotNull( profile.getJdk() );
        assertEquals( 1, profile.getEnvironmentVariables().size() );

        getProfileService().removeInstallationFromProfile( profile, jdk2 );

        profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertNull( profile.getJdk() );
        assertEquals( 1, profile.getEnvironmentVariables().size() );

        getProfileService().removeInstallationFromProfile( profile, mvnOpts2 );
        profile = getProfileService().getProfile( jdk1mvn205.getId() );
        assertNull( profile.getJdk() );
        assertEquals( 0, profile.getEnvironmentVariables().size() );
    }


}
