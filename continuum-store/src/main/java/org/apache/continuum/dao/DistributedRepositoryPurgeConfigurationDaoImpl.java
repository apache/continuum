package org.apache.continuum.dao;

import org.apache.continuum.model.repository.DistributedRepositoryPurgeConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.springframework.stereotype.Repository;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.util.Collections;
import java.util.List;

@Repository( "distributedRepositoryPurgeConfigurationDao" )
public class DistributedRepositoryPurgeConfigurationDaoImpl
    extends AbstractDao
    implements DistributedRepositoryPurgeConfigurationDao
{
    public List<DistributedRepositoryPurgeConfiguration> getAllDistributedRepositoryPurgeConfigurations()
    {
        return getAllObjectsDetached( DistributedRepositoryPurgeConfiguration.class );
    }

    public DistributedRepositoryPurgeConfiguration addDistributedRepositoryPurgeConfiguration(
        DistributedRepositoryPurgeConfiguration purgeConfiguration )
        throws ContinuumStoreException
    {
        return addObject( purgeConfiguration );
    }

    public void removeDistributedRepositoryPurgeConfiguration(
        DistributedRepositoryPurgeConfiguration purgeConfiguration )
    {
        removeObject( purgeConfiguration );
    }

    public DistributedRepositoryPurgeConfiguration getDistributedRepositoryPurgeConfiguration( int configId )
        throws ContinuumStoreException
    {
        return getObjectById( DistributedRepositoryPurgeConfiguration.class, configId );
    }

    public void updateDistributedRepositoryPurgeConfiguration( DistributedRepositoryPurgeConfiguration purgeConfig )
        throws ContinuumStoreException
    {
        updateObject( purgeConfig );
    }

    public List<DistributedRepositoryPurgeConfiguration> getEnableDistributedRepositoryPurgeConfigurationsBySchedule(
        int scheduleId )
    {
        PersistenceManager pm = getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try
        {
            tx.begin();
            Extent extent = pm.getExtent( DistributedRepositoryPurgeConfiguration.class, true );
            Query query = pm.newQuery( extent );
            query.declareParameters( "int scheduleId" );
            query.setFilter( "this.schedule.id == scheduleId && this.enabled == true" );
            List result = (List) query.execute( scheduleId );
            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();
            rollback( tx );
        }
    }

}