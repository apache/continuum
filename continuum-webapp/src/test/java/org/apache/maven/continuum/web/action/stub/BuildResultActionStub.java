package org.apache.maven.continuum.web.action.stub;

import org.apache.maven.continuum.web.action.BuildResultAction;

public class BuildResultActionStub
    extends BuildResultAction
{
    public String getProjectGroupName()
    {
        return "test-group";
    }
    
    protected void checkViewProjectGroupAuthorization( String resource )
    {
        // skip authorization check
    }
}
