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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="checkout-project"
 */
public class CheckoutProjectContinuumAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifier;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    public void execute( Map context )
        throws Exception
    {
        Project project = getProject( context );

        int oldState = project.getState();

        BuildDefinition buildDefinition = getBuildDefinition( context );

        project.setState( ContinuumProjectState.CHECKING_OUT );

        store.updateProject( project );

        File workingDirectory = getWorkingDirectory( context );

        // ----------------------------------------------------------------------
        // Check out the project
        // ----------------------------------------------------------------------

        ScmResult result;

        try
        {
            result = scm.checkOut( project, workingDirectory, context );
            //CONTINUUM-1394
            result.setChanges( null );
        }
        catch ( ContinuumScmException e )
        {
            // TODO: Dissect the scm exception to be able to give better feedback
            Throwable cause = e.getCause();

            if ( cause instanceof NoSuchScmProviderException )
            {
                result = new ScmResult();

                result.setSuccess( false );

                result.setProviderMessage( cause.getMessage() );
            }
            else if ( e.getResult() != null )
            {
                result = e.getResult();
            }
            else
            {
                result = new ScmResult();

                result.setSuccess( false );

                result.setException( ContinuumUtils.throwableMessagesToString( e ) );
            }
        }
        catch ( Throwable t )
        {
            // TODO: do we want this here, or should it be to the logs?
            result = new ScmResult();

            result.setSuccess( false );

            result.setException( ContinuumUtils.throwableMessagesToString( t ) );
        }
        finally
        {
            if ( oldState == ContinuumProjectState.NEW )
            {
                String relativePath = (String) getObject( context, KEY_PROJECT_RELATIVE_PATH, "" );
                if ( StringUtils.isNotEmpty( relativePath ) )
                {
                    //CONTINUUM-1218 : updating only the default build definition only for new projects
                    BuildDefinition bd = continuum.getDefaultBuildDefinition( project.getId() );

                    String buildFile = "";
                    if (ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR.equals( project.getExecutorId() ) )
                    {
                        buildFile = "pom.xml";
                        bd.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
                    }
                    else if ( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR.equals( project.getExecutorId() ) )
                    {
                        buildFile = "project.xml";
                        bd.setType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );
                    }
                    else if ( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR.equals( project.getExecutorId() ) )
                    {
                        buildFile = "build.xml";
                        bd.setType( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
                    }
                    else
                    {
                        bd.setType( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );
                    }
                    bd.setBuildFile( relativePath + "/" + "buildFile" );
                    store.storeBuildDefinition( bd );
                }
            }
            project.setState( ContinuumProjectState.CHECKEDOUT );

            store.updateProject( project );

            notifier.checkoutComplete( project, buildDefinition );
        }

        context.put( KEY_CHECKOUT_SCM_RESULT, result );
    }
}
