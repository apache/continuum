package org.apache.continuum.release.phase;

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

import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.continuum.utils.shell.ShellCommandHelper;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.phase.AbstractRunGoalsPhase;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 */
public abstract class AbstractContinuumRunGoalsPhase
    extends AbstractRunGoalsPhase
{
    /**
     * @plexus.requirement
     */
    private ShellCommandHelper shellCommandHelper;

    /**
     * @plexus.requirement
     */
    private InstallationService installationService;

    /**
     * TODO olamy use maven-invoker with an installation (jdk, mvn path, env var)
     */
    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, File workingDirectory,
                                  String additionalArguments )
        throws ReleaseExecutionException
    {
        ReleaseResult result = new ReleaseResult();

        try
        {
            String goals = getGoals( releaseDescriptor );
            if ( !StringUtils.isEmpty( goals ) )
            {
                Map<String, String> environments = null;

                String executable = null;

                if ( releaseDescriptor instanceof ContinuumReleaseDescriptor )
                {
                    environments = ( (ContinuumReleaseDescriptor) releaseDescriptor ).getEnvironments();

                    executable = ( (ContinuumReleaseDescriptor) releaseDescriptor ).getExecutable();
                }
                shellCommandHelper.executeGoals( determineWorkingDirectory( workingDirectory,
                                                                            releaseDescriptor.getScmRelativePathProjectDirectory() ),
                                                 executable, goals, releaseDescriptor.isInteractive(),
                                                 additionalArguments, result, environments );
            }
        }
        catch ( Exception e )
        {
            throw new ReleaseExecutionException( result.getOutput(), e );
        }

        result.setResultCode( ReleaseResult.SUCCESS );

        return result;
    }

    @Override
    public ReleaseResult execute( ReleaseDescriptor arg0, ReleaseEnvironment arg1, File arg2, String arg3 )
        throws ReleaseExecutionException
    {
        return super.execute( arg0, arg1, arg2, arg3 );
    }

    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                  List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {

        return execute( releaseDescriptor, new File( releaseDescriptor.getWorkingDirectory() ),
                        releaseDescriptor.getAdditionalArguments() );
    }

    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment,
                                   List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        return execute( releaseDescriptor, new File( releaseDescriptor.getWorkingDirectory() ),
                        releaseDescriptor.getAdditionalArguments() );
    }
}
