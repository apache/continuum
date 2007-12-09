/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.store.api.Store;

/**
 * Bean factory that is used by Spring container to create and return instances of {@link Store} implementations.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaStoreFactory
{

    /**
     * Store instance that services executes requests on underlying store for {@link Project} entities.
     */
    private final JpaStore<Project, ProjectQuery<Project>> JPA_PROJECT_STORE =
        new JpaStore<Project, ProjectQuery<Project>>();

    /**
     * Store instance that services executes requests on underlying store for {@link ProjectGroup} entities.
     */
    private final JpaStore<ProjectGroup, ProjectGroupQuery<ProjectGroup>> JPA_PROJECT_GROUP_STORE =
        new JpaStore<ProjectGroup, ProjectGroupQuery<ProjectGroup>>();

    /**
     * Store instance that services executes requests on underlying store for {@link ProjectNotifier} entities.
     */
    private final JpaStore<ProjectNotifier, ProjectNotifierQuery<ProjectNotifier>> JPA_PROJECT_NOTIFIER_STORE =
        new JpaStore<ProjectNotifier, ProjectNotifierQuery<ProjectNotifier>>();

    /**
     * Returns a {@link Store} instance to service {@link Project} Entity.
     * 
     * @return a {@link Store} instance to service {@link Project} Entity
     */
    public Store<Project, ProjectQuery<Project>> createProjectGroupStoreInstance()
    {
        return JPA_PROJECT_STORE;
    }

    /**
     * Returns a {@link Store} instance to service {@link ProjectGroup} Entity.
     * 
     * @return a {@link Store} instance to service {@link ProjectGroup} Entity.
     */
    public Store<ProjectGroup, ProjectGroupQuery<ProjectGroup>> createProjectStoreInstance()
    {
        return JPA_PROJECT_GROUP_STORE;
    }

    /**
     * Returns a {@link Store} instance to service {@link ProjectNotifier} Entity.
     * 
     * @return a {@link Store} instance to service {@link ProjectNotifier} Entity.
     */
    public Store<ProjectNotifier, ProjectNotifierQuery<ProjectNotifier>> createProjectNotifierStoreInstance()
    {

        return JPA_PROJECT_NOTIFIER_STORE;
    }

}
