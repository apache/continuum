package org.apache.maven.continuum.web.action.stub;

import org.apache.maven.continuum.web.action.ProjectGroupAction;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.users.jdo.JdoUser;

public class ProjectGroupActionStub
    extends ProjectGroupAction
{
    public String getProjectGroupName()
    {
        return "test-group";
    }

    protected void checkViewProjectGroupAuthorization( String resource )
    {
        // skip authorization check
    }

    protected User getUser( String principal )
        throws UserNotFoundException
    {
        User user = new JdoUser();
        user.setUsername( principal );

        return user;
    }
}
