/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.api.ProjectQuery;
import org.apache.maven.continuum.store.api.Store;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaStoreFactory
{

    private final JpaStore<Project, ProjectQuery<Project>> JPA_PROJECT_STORE =
        new JpaStore<Project, ProjectQuery<Project>>();

    public Store<Project, ProjectQuery<Project>> createProjectStoreInstance()
    {
        return JPA_PROJECT_STORE;
    }

}
