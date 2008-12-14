package org.apache.continuum.buildagent.buildcontext.manager;

import java.util.List;

import org.apache.continuum.buildagent.model.BuildContext;

/**
 * @author Jan Stevens Ancajas
 */
public interface BuildContextManager
{
    String ROLE = BuildContextManager.class.getName();
    
    public void setBuildContextList(List<BuildContext> buildContext);
    
    public List<BuildContext> getBuildContextList();
    
    public BuildContext getBuildContext(int projectId);
}
