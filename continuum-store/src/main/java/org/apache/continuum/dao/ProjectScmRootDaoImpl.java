package org.apache.continuum.dao;

import java.util.Collection;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="org.apache.continuum.dao.ProjectScmRootDao"
 */
public class ProjectScmRootDaoImpl
    extends AbstractDao
    implements ProjectScmRootDao
{

    public ProjectScmRoot addProjectScmRoot( ProjectScmRoot projectScmRoot )
        throws ContinuumStoreException
    {
        return (ProjectScmRoot) addObject( projectScmRoot );
    }

    public List<ProjectScmRoot> getAllProjectScmRoots()
    {
        return getAllObjectsDetached( ProjectScmRoot.class );
    }
    
    public List<ProjectScmRoot> getProjectScmRootByProjectGroup( int projectGroupId )
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ProjectScmRoot.class, true );

            Query query = pm.newQuery( extent, "projectGroup.id == " + projectGroupId );

            List result = (List) query.execute();

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public void removeProjectScmRoot( ProjectScmRoot projectScmRoot )
        throws ContinuumStoreException
    {
        removeObject( projectScmRoot );
    }

    public void updateProjectScmRoot( ProjectScmRoot projectScmRoot )
        throws ContinuumStoreException
    {
        updateObject( projectScmRoot );
    }

    public ProjectScmRoot getProjectScmRootByProjectGroupAndScmRootAddress( int projectGroupId, String scmRootAddress )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ProjectScmRoot.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "int projectGroupId, String scmRootAddress" );

            query.setFilter( "this.projectGroup.id == projectGroupId && this.scmRootAddress == scmRootAddress" );

            Object[] params = new Object[2];
            params[0] = projectGroupId;
            params[1] = scmRootAddress;
            
            Collection result = (Collection) query.executeWithArray( params );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (ProjectScmRoot) object;
        }
        finally
        {
            rollback( tx );
        }
    }
    
}
