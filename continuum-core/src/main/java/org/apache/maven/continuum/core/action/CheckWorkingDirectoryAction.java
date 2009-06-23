package org.apache.maven.continuum.core.action;

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

import java.io.File;
import java.util.Map;

import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.utils.WorkingDirectoryService;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="check-working-directory"
 */
public class CheckWorkingDirectoryAction
    extends AbstractContinuumAction
{
    private static final String KEY_WORKING_DIRECTORY_EXISTS = "working-directory-exists";

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    public void execute( Map context )
        throws Exception
    {
        Project project = projectDao.getProject( getProjectId( context ) );

        File workingDirectory = workingDirectoryService.getWorkingDirectory( project );

        if ( !workingDirectory.exists() )
        {
            setWorkingDirectoryExist( context, false );

            return;
        }

        File[] files = workingDirectory.listFiles();

        if ( files == null )
        {
            //workingDirectory isn't a directory but a file. Not possible in theory.
            String msg = workingDirectory.getAbsolutePath() + " isn't a directory but a file.";
            getLogger().error( msg );
            throw new ContinuumException( msg );
        }

        setWorkingDirectoryExist( context, files.length > 0 );
    }

    public static boolean isWorkingDirectoryExist( Map<String, Object> context )
    {
        return getBoolean( context, KEY_WORKING_DIRECTORY_EXISTS );
    }

    private static void setWorkingDirectoryExist( Map<String, Object> context, boolean exists )
    {
        context.put( KEY_WORKING_DIRECTORY_EXISTS, exists );
    }
}
