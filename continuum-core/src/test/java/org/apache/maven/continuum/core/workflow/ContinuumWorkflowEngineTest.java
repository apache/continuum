package org.apache.maven.continuum.core.workflow;

/*
 * Copyright 2006 The Apache Software Foundation.
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
 *
 */

import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.HashMap;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumWorkflowEngineTest
    extends PlexusTestCase
{
    public void testAddMavenTwoProjectWithWarnings()
        throws Exception
    {
        ContinuumWorkflowEngine workflowEngine = (ContinuumWorkflowEngine) lookup( ContinuumWorkflowEngine.ROLE );

        File pom = getTestFile( "src/test/resources/projects/pom-with-warnings.xml" );

        long workflowId = workflowEngine.addProjectsFromMetadata( null, MavenTwoContinuumProjectBuilder.ID,
                                                                  pom.toURL().toExternalForm(),
                                                                  getTestPath( "target/workingdirectory" ), true );

        System.out.println( "workflowId = " + workflowId );
        // TODO: Assert projects, projectGroups and warnings lists from the context

        workflowEngine.executeAction( workflowId, 2, new HashMap() );
    }
}
