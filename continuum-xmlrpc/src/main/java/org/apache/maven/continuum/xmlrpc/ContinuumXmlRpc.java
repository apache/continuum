package org.apache.maven.continuum.xmlrpc;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
 */

import java.util.Hashtable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo - do we need the per-type variations?
 */
public interface ContinuumXmlRpc
{
    String ROLE = ContinuumXmlRpc.class.getName();

    // ----------------------------------------------------------------------
    // Keep these methods organized in the same order as the Continuum
    // interface
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    Hashtable removeProject( int projectId );

    Hashtable getProject( int projectId );

    Hashtable getProjects();

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    Hashtable buildProject( int projectId, int trigger );

    // ----------------------------------------------------------------------
    // Builds
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    Hashtable addMavenTwoProject( String url );

    Hashtable addMavenTwoProject( Hashtable mavenTwoProject );

    Hashtable updateMavenTwoProject( Hashtable mavenTwoProject );

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    Hashtable addMavenOneProject( String url );

    Hashtable addMavenOneProject( Hashtable mavenOneProject );

    Hashtable updateMavenOneProject( Hashtable mavenOneProject );

    // ----------------------------------------------------------------------
    // Ant projects
    // ----------------------------------------------------------------------

    Hashtable addAntProject( Hashtable antProject );

    Hashtable updateAntProject( Hashtable antProject );

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    Hashtable addShellProject( Hashtable shellProject );

    Hashtable updateShellProject( Hashtable shellProject );
}
