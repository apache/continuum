package org.apache.maven.continuum.core.action;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StoreProjectAction
    extends AbstractContinuumAction
{
    public void execute( Map context )
        throws ContinuumException, ContinuumStoreException
    {
        ContinuumProject project = getUnvalidatedProject( context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = getStore().addProject( project );

        context.put( KEY_PROJECT_ID, project.getId() );

        // ----------------------------------------------------------------------
        // Set the working directory
        // ----------------------------------------------------------------------

        File projectWorkingDirectory = new File( getCore().getWorkingDirectory(), project.getId() );

        if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
        {
            throw new ContinuumException( "Could not make the working directory for the project " +
                                          "'" + projectWorkingDirectory.getAbsolutePath() + "'." );
        }

        // The working directory is created based on the project id so we can always
        // figure out what it is.

        project.setWorkingDirectory( projectWorkingDirectory.getAbsolutePath() );

        project.setCommandLineArguments( StringUtils.clean( project.getCommandLineArguments() ) );

        getStore().updateProject( project );
    }
}
