package org.apache.maven.continuum.web.action.stub;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.web.action.AddMavenProjectAction;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;

/**
 * A stubbed implementation of {@link org.apache.maven.continuum.web.action.AddMavenProjectAction} useful for testing
 * the abstract class's functionality.
 */
public class AddMavenProjectStub
    extends AddMavenProjectAction
{
    @Override
    protected void checkAddProjectGroupAuthorization()
        throws AuthorizationRequiredException
    {
        // skip authorization check
    }
    
    @Override
    protected ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup, boolean checkProtocol,
                                                        boolean scmUseCache )
        throws ContinuumException
    {
        return new ContinuumProjectBuildingResult();
    }
}
