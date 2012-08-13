package org.apache.continuum.web.action;

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.apache.struts2.ServletActionContext;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="scmResult"
 */
public class ScmResultAction
    extends ContinuumActionSupport
{
    private int projectGroupId;

    private int projectScmRootId;

    private String projectGroupName;

    private String state;

    private ProjectScmRoot projectScmRoot;

    public String execute()
        throws Exception
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        projectScmRoot = getContinuum().getProjectScmRoot( projectScmRootId );

        state = StateGenerator.generate( projectScmRoot.getState(),
                                         ServletActionContext.getRequest().getContextPath() );

        return SUCCESS;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getProjectScmRootId()
    {
        return projectScmRootId;
    }

    public void setProjectScmRootId( int projectScmRootId )
    {
        this.projectScmRootId = projectScmRootId;
    }

    public ProjectScmRoot getProjectScmRoot()
    {
        return projectScmRoot;
    }

    public void setProjectScmRoot( ProjectScmRoot projectScmRoot )
    {
        this.projectScmRoot = projectScmRoot;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        projectGroupName = getContinuum().getProjectGroup( getProjectGroupId() ).getName();

        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public String getState()
    {
        return state;
    }

    public void setState( String state )
    {
        this.state = state;
    }
}
