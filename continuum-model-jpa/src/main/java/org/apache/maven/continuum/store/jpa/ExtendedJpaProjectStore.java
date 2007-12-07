/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.List;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.api.ProjectQuery;
import org.apache.maven.continuum.store.api.StoreException;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @deprecated <em>experimental</em>
 */
public class ExtendedJpaProjectStore<T extends Project, Q extends ProjectQuery<Project>>
    extends JpaStore<Project, ProjectQuery<Project>>
{

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.jpa.JpaStore#query(org.apache.maven.continuum.store.api.Query)
     */
    @Override
    public List<Project> query( ProjectQuery<Project> query ) throws StoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
