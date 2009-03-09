package org.apache.maven.continuum.project.builder.maven;

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

import org.apache.log4j.Logger;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenTwoContinuumProjectBuilderTest
    extends AbstractContinuumTest
{

    private Logger logger = Logger.getLogger( getClass() );

    /*
    protected String getConfigurationName( String subname )
        throws Exception
    {
        return super.getConfigurationName( subname );
    }
    */

    protected String getCustomConfigurationName()
    {
        return "plexus.xml";
    }

    public void testGetEmailAddressWhenTypeIsSetToEmail()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-1.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null );

        assertNotNull( result.getErrors() );

        assertEquals( 0, result.getErrors().size() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );

        assertNotNull( project );

        assertNotNull( project.getNotifiers() );

        assertEquals( 1, project.getNotifiers().size() );

        ProjectNotifier notifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "mail", notifier.getType() );

        assertEquals( "foo@bar", notifier.getConfiguration().get( "address" ) );

        ProjectGroup pg = result.getProjectGroups().get( 0 );

        assertNotNull( pg );

        assertNotNull( pg.getNotifiers() );

        assertEquals( 0, pg.getNotifiers().size() );
    }

    public void testGetEmailAddressWhenTypeIsntSet()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-2.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null );

        assertNotNull( result.getErrors() );

        assertEquals( 0, result.getErrors().size() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        Project project = result.getProjects().get( 0 );

        assertNotNull( project );

        assertNotNull( project.getNotifiers() );

        assertEquals( 1, project.getNotifiers().size() );

        ProjectNotifier notifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "mail", notifier.getType() );

        assertEquals( "foo@bar", notifier.getConfiguration().get( "address" ) );

        ProjectGroup pg = result.getProjectGroups().get( 0 );

        assertNotNull( pg );

        assertNotNull( pg.getNotifiers() );

        assertEquals( 0, pg.getNotifiers().size() );
    }

    public void testGetScmUrlWithParams()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-3.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null );

        assertNotNull( result.getErrors() );

        assertEquals( 0, result.getErrors().size() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        ProjectGroup pg = result.getProjectGroups().get( 0 );

        assertNotNull( pg );

        String username = System.getProperty( "user.name" );

        String scmUrl = "scm:cvs:ext:${user.name}@company.org:/home/company/cvs:project/foo";

        Project project = result.getProjects().get( 0 );

        scmUrl = StringUtils.replace( scmUrl, "${user.name}", username );

        assertEquals( scmUrl, project.getScmUrl() );
    }

    public void testCreateProjectsWithModules()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        URL url = getClass().getClassLoader().getResource( "projects/continuum/pom.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( url, null, null );

        assertNotNull( result );

        // ----------------------------------------------------------------------
        // Assert the warnings
        // ----------------------------------------------------------------------

        assertNotNull( result.getErrors() );

        assertEquals( 1, result.getErrors().size() );

        assertEquals( ContinuumProjectBuildingResult.ERROR_POM_NOT_FOUND, result.getErrors().get( 0 ) );

        // ----------------------------------------------------------------------
        // Assert the project group built
        // ----------------------------------------------------------------------

        assertNotNull( result.getProjectGroups() );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = result.getProjectGroups().iterator().next();

        assertEquals( "projectGroup.groupId", "org.apache.maven.continuum", projectGroup.getGroupId() );

        assertEquals( "projectGroup.name", "Continuum Parent Project", projectGroup.getName() );

        assertEquals( "projectGroup.description", "Continuum Project Description", projectGroup.getDescription() );

        // assertEquals( "projectGroup.url", "http://cvs.continuum.codehaus.org/", projectGroup.getUrl() );

        // ----------------------------------------------------------------------
        // Assert the projects built
        // ----------------------------------------------------------------------

        assertNotNull( result.getProjects() );

        assertEquals( 9, result.getProjects().size() );

        Map<String, Project> projects = new HashMap<String, Project>();

        for ( Project project : result.getProjects() )
        {
            assertNotNull( project.getName() );

            assertNotNull( project.getDescription() );

            projects.put( project.getName(), project );
        }

        assertMavenTwoProject( "Continuum Core", projects );
        assertMavenTwoProject( "Continuum Model", projects );
        assertMavenTwoProject( "Continuum Plexus Application", projects );
        assertScmUrl( "Continuum Web", projects,
                      "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/tags/continuum-1.0.3/continuum-web" );
        //directoryName != artifactId for this project
        assertScmUrl( "Continuum XMLRPC Interface", projects,
                      "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/tags/continuum-1.0.3/continuum-xmlrpc" );
        assertMavenTwoProject( "Continuum Notifiers", projects );
        assertMavenTwoProject( "Continuum IRC Notifier", projects );
        assertMavenTwoProject( "Continuum Jabber Notifier", projects );

        assertEquals( "continuum-parent-notifiers",
                      ( projects.get( "Continuum IRC Notifier" ) ).getParent().getArtifactId() );

        assertEquals( "continuum-parent-notifiers",
                      ( projects.get( "Continuum Jabber Notifier" ) ).getParent().getArtifactId() );

        assertDependency( "Continuum Model", "Continuum Web", projects );
    }

    public void testCreateProjectsWithModuleswithParentPomIsntPomXml()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        URL url = getClass().getClassLoader().getResource( "projects/continuum/pom_ci.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( url, null, null );

        assertNotNull( result );

        // ----------------------------------------------------------------------
        // Assert the warnings
        // ----------------------------------------------------------------------

        assertNotNull( result.getErrors() );

        assertEquals( 1, result.getErrors().size() );

        assertEquals( ContinuumProjectBuildingResult.ERROR_POM_NOT_FOUND, result.getErrors().get( 0 ).toString() );

        // ----------------------------------------------------------------------
        // Assert the project group built
        // ----------------------------------------------------------------------

        assertNotNull( result.getProjectGroups() );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = (ProjectGroup) result.getProjectGroups().iterator().next();

        assertEquals( "projectGroup.groupId", "org.apache.maven.continuum", projectGroup.getGroupId() );

        assertEquals( "projectGroup.name", "Continuum Parent Project", projectGroup.getName() );

        assertEquals( "projectGroup.description", "Continuum Project Description", projectGroup.getDescription() );

        // assertEquals( "projectGroup.url", "http://cvs.continuum.codehaus.org/", projectGroup.getUrl() );

        // ----------------------------------------------------------------------
        // Assert the projects built
        // ----------------------------------------------------------------------

        assertNotNull( result.getProjects() );

        assertEquals( 9, result.getProjects().size() );

        Map projects = new HashMap();

        for ( Iterator it = result.getProjects().iterator(); it.hasNext(); )
        {
            Project project = (Project) it.next();

            assertNotNull( project.getName() );

            projects.put( project.getName(), project );
        }

        assertMavenTwoProject( "Continuum Core", projects );
        assertMavenTwoProject( "Continuum Model", projects );
        assertMavenTwoProject( "Continuum Plexus Application", projects );
        assertScmUrl( "Continuum Web", projects,
                      "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/tags/continuum-1.0.3/continuum-web" );
        //directoryName != artifactId for this project
        assertScmUrl( "Continuum XMLRPC Interface", projects,
                      "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/tags/continuum-1.0.3/continuum-xmlrpc" );
        assertMavenTwoProject( "Continuum Notifiers", projects );
        assertMavenTwoProject( "Continuum IRC Notifier", projects );
        assertMavenTwoProject( "Continuum Jabber Notifier", projects );

        assertEquals( "continuum-parent-notifiers",
                      ( (Project) projects.get( "Continuum IRC Notifier" ) ).getParent().getArtifactId() );

        assertEquals( "continuum-parent-notifiers",
                      ( (Project) projects.get( "Continuum Jabber Notifier" ) ).getParent().getArtifactId() );

        assertDependency( "Continuum Model", "Continuum Web", projects );
    }

    public void testCreateProjectWithoutModules()
        throws Exception
    {

        ContinuumProjectBuilder projectBuilder =
            (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        URL url = getClass().getClassLoader().getResource( "projects/continuum/continuum-core/pom.xml" );

        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( "clean test-compile" );

        bd.setArguments( "-N" );

        bd.setBuildFile( "pom.xml" );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );

        BuildDefinitionService service = (BuildDefinitionService) lookup( BuildDefinitionService.class );

        bd = service.addBuildDefinition( bd );
        BuildDefinitionTemplate bdt = new BuildDefinitionTemplate();
        bdt.setName( "maven2" );
        bdt = service.addBuildDefinitionTemplate( bdt );
        bdt = service.addBuildDefinitionInTemplate( bdt, bd, false );
        assertEquals( 5, service.getAllBuildDefinitionTemplate().size() );
        logger.debug( "templates number " + service.getAllBuildDefinitionTemplate().size() );

        logger.debug( "projectGroups number " + getProjectGroupDao().getAllProjectGroups().size() );

        int all = service.getAllBuildDefinitions().size();

        ContinuumProjectBuildingResult result;

        result = projectBuilder.buildProjectsFromMetadata( url, null, null, false, bdt );

        assertEquals( 5, service.getAllBuildDefinitionTemplate().size() );

        assertEquals( all + 1, service.getAllBuildDefinitions().size() );

        assertNotNull( result );

        assertNotNull( result.getErrors() );

        assertEquals( 0, result.getErrors().size() );

        assertNotNull( result.getProjectGroups() );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = (ProjectGroup) result.getProjectGroups().get( 0 );

        assertEquals( "projectGroup.groupId", "org.apache.maven.continuum", projectGroup.getGroupId() );

        assertEquals( "projectGroup.name", "Continuum Core", projectGroup.getName() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        assertNotNull( projectGroup.getProjects() );

        assertEquals( 0, projectGroup.getProjects().size() );
    }

    private void assertDependency( String dep, String proj, Map projects )
    {
        Project p = (Project) projects.get( proj );

        Project dependency = (Project) projects.get( dep );

        assertNotNull( p );

        assertNotNull( dependency );

        assertNotNull( p.getDependencies() );

        for ( Iterator i = p.getDependencies().iterator(); i.hasNext(); )
        {
            ProjectDependency pd = (ProjectDependency) i.next();

            if ( pd.getArtifactId().equals( dependency.getArtifactId() ) &&
                pd.getGroupId().equals( dependency.getGroupId() ) && pd.getVersion().equals( dependency.getVersion() ) )
            {
                return;
            }
        }

        assertFalse( true );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Project getProject( String name, Map projects )
    {
        return (Project) projects.get( name );
    }

    private void assertMavenTwoProject( String name, Map projects )
    {
        Project project = getProject( name, projects );

        assertNotNull( project );

        assertEquals( name, project.getName() );

        String scmUrl = "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/";

        assertTrue( project.getScmUrl().startsWith( scmUrl ) );
    }

    private void assertScmUrl( String name, Map projects, String scmUrl )
    {
        assertMavenTwoProject( name, projects );

        Project project = getProject( name, projects );

        assertEquals( scmUrl, project.getScmUrl() );
    }
}
