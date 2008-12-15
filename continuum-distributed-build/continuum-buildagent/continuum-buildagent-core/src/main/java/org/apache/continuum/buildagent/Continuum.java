package org.apache.continuum.buildagent;

import java.util.List;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.maven.continuum.ContinuumException;

public interface Continuum
{
    String ROLE = Continuum.class.getName();

    void prepareBuildProjects( List<BuildContext> buildContextList )
        throws ContinuumException;
}
