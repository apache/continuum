package org.apache.maven.continuum.web.action.stub;

import org.apache.maven.continuum.web.action.SummaryAction;

public class SummaryActionStub
    extends SummaryAction
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
