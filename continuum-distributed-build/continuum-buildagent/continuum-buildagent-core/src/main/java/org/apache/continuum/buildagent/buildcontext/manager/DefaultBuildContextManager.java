package org.apache.continuum.buildagent.buildcontext.manager;

import java.util.List;

import org.apache.continuum.buildagent.buildcontext.BuildContext;

/**
 * @author Jan Steven Ancajas
 * @plexus.component role="org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager"
 */
public class DefaultBuildContextManager
    implements BuildContextManager
{
    public List<BuildContext> buildContexts;

    public BuildContext getBuildContext( int projectId )
    {
        BuildContext context = null;

        if (buildContexts!= null)
        {
            for ( BuildContext item : buildContexts )
            {
                if (item.getProjectId() == projectId)
                {
                    context = item;
                    break;
                }
            }
        }

        return context;
    }

    public List<BuildContext> getBuildContextList()
    {
        return buildContexts;
    }

    public void setBuildContextList( List<BuildContext> buildContexts )
    {
        this.buildContexts = buildContexts;
    }
}