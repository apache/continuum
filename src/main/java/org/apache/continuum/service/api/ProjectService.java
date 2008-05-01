package org.apache.continuum.service.api;

import org.apache.continuum.model.project.Project;
import org.apache.continuum.model.project.ProjectNotifier;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface ProjectService
{
    Project saveOrUpdate( Project project );

    Project getProject( long projecId );

    Project getProject( String groupId, String artifactId, String version );

    List<ProjectNotifier> getNotifiers( Project p );

    void addNotifier( Project p, ProjectNotifier notifier );

    void removeNotifier( Project p, ProjectNotifier notifier );

    //BuildDefinition getDefaultBuildDefinition( Project p );

    //BuildResult buildProject( Project p );

    //BuildResult buildProject( Project p, BuildDefinition bd );

    //BuildResult buildProject( Project p, BuildDefinition bd, boolean force );
}
