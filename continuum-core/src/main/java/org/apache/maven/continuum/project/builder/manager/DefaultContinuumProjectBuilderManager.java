package org.apache.maven.continuum.project.builder.manager;

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

import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager"
 * role-hint="default"
 */
public class DefaultContinuumProjectBuilderManager
    extends AbstractLogEnabled
    implements ContinuumProjectBuilderManager
{
    /**
     * @plexus.requirement role="org.apache.maven.continuum.project.builder.ContinuumProjectBuilder"
     */
    private Map projectBuilders;

    // ----------------------------------------------------------------------
    // ProjectCreatorManager Implementation
    // ----------------------------------------------------------------------

    public ContinuumProjectBuilder getProjectBuilder( String id )
        throws ContinuumProjectBuilderManagerException
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) projectBuilders.get( id );

        if ( projectBuilder == null )
        {
            throw new ContinuumProjectBuilderManagerException( "No such project creator with id '" + id + "'." );
        }

        return projectBuilder;
    }
}
