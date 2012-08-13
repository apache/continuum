package org.apache.continuum.xmlrpc.server;

import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.server.ContinuumServiceImpl;

public class ContinuumServiceImplStub
    extends ContinuumServiceImpl
{
    protected void checkBuildProjectInGroupAuthorization( String resource )
        throws ContinuumException
    {
        // do nothing
    }

    protected void checkViewProjectGroupAuthorization( String resource )
        throws ContinuumException
    {
        // do nothing
    }

    protected void buildProjectWithBuildDefinition( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
    {
        // do nothing
    }

    public org.apache.maven.continuum.model.project.BuildDefinition getBuildDefinition( BuildDefinition buildDef,
                                                                                        org.apache.maven.continuum.model.project.BuildDefinition buildDefinition )
        throws ProfileException, ContinuumException
    {
        return populateBuildDefinition( buildDef, buildDefinition );
    }
}
