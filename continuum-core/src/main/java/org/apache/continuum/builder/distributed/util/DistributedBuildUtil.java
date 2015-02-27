package org.apache.continuum.builder.distributed.util;

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

import org.apache.continuum.builder.utils.ContinuumBuildConstant;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component( role = org.apache.continuum.builder.distributed.util.DistributedBuildUtil.class )
public class DistributedBuildUtil
{
    private Logger log = LoggerFactory.getLogger( DistributedBuildUtil.class );

    @Requirement
    private ProjectDao projectDao;

    @Requirement
    private BuildResultDao buildResultDao;

    public BuildResult convertMapToBuildResult( Map<String, Object> context )
    {
        BuildResult buildResult = new BuildResult();

        updateBuildResultFromMap( buildResult, context );

        return buildResult;
    }

    public void updateBuildResultFromMap( BuildResult buildResult, Map<String, Object> context )
    {
        buildResult.setStartTime( ContinuumBuildConstant.getStartTime( context ) );
        buildResult.setEndTime( ContinuumBuildConstant.getEndTime( context ) );
        buildResult.setError( ContinuumBuildConstant.getBuildError( context ) );
        buildResult.setExitCode( ContinuumBuildConstant.getBuildExitCode( context ) );
        buildResult.setState( ContinuumBuildConstant.getBuildState( context ) );
        buildResult.setTrigger( ContinuumBuildConstant.getTrigger( context ) );
        buildResult.setUsername( ContinuumBuildConstant.getUsername( context ) );
        buildResult.setBuildUrl( ContinuumBuildConstant.getBuildAgentUrl( context ) );
    }

    public List<ProjectDependency> getModifiedDependencies( BuildResult oldBuildResult, Map<String, Object> context )
        throws ContinuumException
    {
        if ( oldBuildResult == null )
        {
            return null;
        }

        try
        {
            Project project = projectDao.getProjectWithAllDetails( ContinuumBuildConstant.getProjectId( context ) );
            List<ProjectDependency> dependencies = project.getDependencies();

            if ( dependencies == null )
            {
                dependencies = new ArrayList<ProjectDependency>();
            }

            if ( project.getParent() != null )
            {
                dependencies.add( project.getParent() );
            }

            if ( dependencies.isEmpty() )
            {
                return null;
            }

            List<ProjectDependency> modifiedDependencies = new ArrayList<ProjectDependency>();

            for ( ProjectDependency dep : dependencies )
            {
                Project dependencyProject = projectDao.getProject( dep.getGroupId(), dep.getArtifactId(),
                                                                   dep.getVersion() );

                if ( dependencyProject != null )
                {
                    long nbBuild = buildResultDao.getNbBuildResultsInSuccessForProject( dependencyProject.getId(),
                                                                                        oldBuildResult.getEndTime() );
                    if ( nbBuild > 0 )
                    {
                        log.debug( "Dependency changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                                       dep.getVersion() );
                        modifiedDependencies.add( dep );
                    }
                    else
                    {
                        log.debug( "Dependency not changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                                       dep.getVersion() );
                    }
                }
                else
                {
                    log.debug( "Skip non Continuum project: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                                   dep.getVersion() );
                }
            }

            return modifiedDependencies;
        }
        catch ( ContinuumStoreException e )
        {
            log.warn( "Can't get the project dependencies", e );
        }

        return null;
    }

    public ScmResult getScmResult( Map<String, Object> context )
    {
        Map<String, Object> map = ContinuumBuildConstant.getScmResult( context );

        if ( !map.isEmpty() )
        {
            ScmResult scmResult = new ScmResult();
            scmResult.setCommandLine( ContinuumBuildConstant.getScmCommandLine( map ) );
            scmResult.setCommandOutput( ContinuumBuildConstant.getScmCommandOutput( map ) );
            scmResult.setException( ContinuumBuildConstant.getScmException( map ) );
            scmResult.setProviderMessage( ContinuumBuildConstant.getScmProviderMessage( map ) );
            scmResult.setSuccess( ContinuumBuildConstant.isScmSuccess( map ) );
            scmResult.setChanges( getScmChanges( map ) );

            return scmResult;
        }

        return null;
    }

    public List<ChangeSet> getScmChanges( Map<String, Object> context )
    {
        List<ChangeSet> changes = new ArrayList<ChangeSet>();
        List<Map<String, Object>> scmChanges = ContinuumBuildConstant.getScmChanges( context );

        if ( scmChanges != null )
        {
            for ( Map<String, Object> map : scmChanges )
            {
                ChangeSet changeSet = new ChangeSet();
                changeSet.setAuthor( ContinuumBuildConstant.getChangeSetAuthor( map ) );
                changeSet.setComment( ContinuumBuildConstant.getChangeSetComment( map ) );
                changeSet.setDate( ContinuumBuildConstant.getChangeSetDate( map ) );
                setChangeFiles( changeSet, map );
                changes.add( changeSet );
            }
        }

        return changes;
    }

    private void setChangeFiles( ChangeSet changeSet, Map<String, Object> context )
    {
        List<Map<String, Object>> changeFiles = ContinuumBuildConstant.getChangeSetFiles( context );

        if ( changeFiles != null )
        {
            for ( Map<String, Object> map : changeFiles )
            {
                ChangeFile changeFile = new ChangeFile();
                changeFile.setName( ContinuumBuildConstant.getChangeFileName( map ) );
                changeFile.setRevision( ContinuumBuildConstant.getChangeFileRevision( map ) );
                changeFile.setStatus( ContinuumBuildConstant.getChangeFileStatus( map ) );

                changeSet.addFile( changeFile );
            }
        }
    }
}
