package org.apache.continuum.service.api;

import org.apache.continuum.model.project.Project;
import org.apache.continuum.model.project.ProjectGroup;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface ProjectGroupService
{
    ProjectGroup saveOrUpdate( ProjectGroup projectGroup );

    ProjectGroup getProjectGroup( long pgId );

    ProjectGroup getProjectGroup( String groupId );

    ProjectGroup addProjectGroup( ProjectGroup pg );

    void removeProjectGroup( ProjectGroup pg );

    void addProject( ProjectGroup pg, Project p );

    void removeProject( ProjectGroup pg, Project p );

    List<Project> getProjects( ProjectGroup pg );

    //BuildResult buildProjectGroup( ProjectGroup pg );

    //BuildResult buildProjectGroup( ProjectGroup, BuildDefinition bd );

    //BuildResult buildProjectGroup( ProjectGroup, BuildDefinition bd, boolean force );
}
