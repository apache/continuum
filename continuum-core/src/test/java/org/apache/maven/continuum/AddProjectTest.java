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
package org.apache.maven.continuum;

import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;

import java.util.Collections;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 12 juin 2008
 * @version $Id$
 */
public class AddProjectTest
    extends AbstractContinuumTest
{
    static final String SCM_USERNAME = "test";
    
    static final String SCM_PASSWORD = ";password";
    
    public void testScmUserNamePasswordNotStoring()
        throws Exception
    {
        String metadataUrl = "http://test:;password@svn.apache.org/repos/asf/continuum/tags/continuum-1.1/continuum-api/pom.xml";
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.ROLE );
        
        ContinuumProjectBuildingResult result = continuum
            .executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID,
            						getDefaultProjectGroup().getId(), false, true, false, -1, false, false );
        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );
        
        // read the project from store
        Project project = continuum.getProject( result.getProjects().get( 0 ).getId());
        assertNull(  project.getScmUsername() );
        assertNull( project.getScmPassword() );
        assertTrue( project.isScmUseCache() );
    }

    public void testScmUserNamePasswordStoring()
        throws Exception
    {
        String metadataUrl = "http://test:;password@svn.apache.org/repos/asf/continuum/tags/continuum-1.1/continuum-api/pom.xml";
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.ROLE );

        ContinuumProjectBuildingResult result = continuum
            .executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID,
            						getDefaultProjectGroup().getId(), false, false, false, -1, false, false );
        assertEquals( Collections.emptyList(), result.getErrors() );

        assertEquals( 1, result.getProjects().size() );

        // read the project from store
        Project project = continuum.getProject( result.getProjects().get( 0 ).getId() );
        assertEquals( SCM_USERNAME, project.getScmUsername() );
        assertEquals( SCM_PASSWORD, project.getScmPassword() );
        assertFalse( project.isScmUseCache() );
    }    
    
    public void testAntProjectScmUserNamePasswordNotStoring()
        throws Exception
    {
        // use same url since we're just going to add the project, not build it
        String scmUrl = "http://test:;password@svn.apache.org/repos/asf/continuum/tags/continuum-1.1/continuum-api/pom.xml";
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.ROLE );
        
        Project project = new Project();
        project.setName( "Sample Ant Project" );
        project.setVersion( "1.0" );
        project.setScmUsername( SCM_USERNAME );
        project.setScmPassword( SCM_PASSWORD );
        project.setScmUrl( scmUrl );        
        project.setScmUseCache( true );
        
        BuildDefinitionService bdService = ( BuildDefinitionService )lookup( BuildDefinitionService.class.getName() );
        
        int projectId =
            continuum.addProject( project, ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR,
                                  getDefaultProjectGroup().getId(),
                                  bdService.getDefaultAntBuildDefinitionTemplate().getId() );
                
        // read the project from store
        Project retrievedProject = continuum.getProject( projectId );
        assertNull(  retrievedProject.getScmUsername() );
        assertNull( retrievedProject.getScmPassword() );
        assertTrue( retrievedProject.isScmUseCache() );
    }
    
    public void testAntProjectScmUserNamePasswordStoring()
        throws Exception
    {
        // use same url since we're just going to add the project, not build it
        String scmUrl = "http://test:;password@svn.apache.org/repos/asf/continuum/tags/continuum-1.1/continuum-api/pom.xml";
        DefaultContinuum continuum = (DefaultContinuum) lookup( Continuum.ROLE );
        
        Project project = new Project();
        project.setName( "Sample Ant Project" );
        project.setVersion( "1.0" );
        project.setScmUsername( SCM_USERNAME );
        project.setScmPassword( SCM_PASSWORD );
        project.setScmUrl( scmUrl );        
        project.setScmUseCache( false );
        
        BuildDefinitionService bdService = ( BuildDefinitionService )lookup( BuildDefinitionService.class.getName() );
        
        int projectId =
            continuum.addProject( project, ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR,
                                  getDefaultProjectGroup().getId(),
                                  bdService.getDefaultAntBuildDefinitionTemplate().getId() );
               
        // read the project from store
        Project retrievedProject = continuum.getProject( projectId );
        assertEquals( SCM_USERNAME, retrievedProject.getScmUsername() );
        assertEquals( SCM_PASSWORD, retrievedProject.getScmPassword() );
        assertFalse( retrievedProject.isScmUseCache() );
    }
    
}
