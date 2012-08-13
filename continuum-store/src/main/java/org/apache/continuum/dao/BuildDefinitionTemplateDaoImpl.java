package org.apache.continuum.dao;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.continuum.dao.BuildDefinitionTemplateDao"
 */
@Repository( "buildDefinitionTemplateDao" )
public class BuildDefinitionTemplateDaoImpl
    extends AbstractDao
    implements BuildDefinitionTemplateDao
{
    public List<BuildDefinitionTemplate> getAllBuildDefinitionTemplate()
        throws ContinuumStoreException
    {
        return getAllObjectsDetached( BuildDefinitionTemplate.class, BUILD_TEMPLATE_BUILD_DEFINITIONS );
    }

    public List<BuildDefinitionTemplate> getContinuumBuildDefinitionTemplates()
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.setFilter( "continuumDefault == true" );
            pm.getFetchPlan().addGroup( BUILD_TEMPLATE_BUILD_DEFINITIONS );
            List result = (List) query.execute();
            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public BuildDefinitionTemplate getBuildDefinitionTemplate( int id )
        throws ContinuumStoreException
    {
        return (BuildDefinitionTemplate) getObjectById( BuildDefinitionTemplate.class, id,
                                                        BUILD_TEMPLATE_BUILD_DEFINITIONS );
    }

    public BuildDefinitionTemplate addBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException
    {
        return (BuildDefinitionTemplate) addObject( buildDefinitionTemplate );
    }

    public BuildDefinitionTemplate updateBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException
    {
        updateObject( buildDefinitionTemplate );

        return buildDefinitionTemplate;
    }

    public void removeBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException
    {
        removeObject( buildDefinitionTemplate );
    }

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplatesWithType( String type )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.declareImports( "import java.lang.String" );
            query.declareParameters( "String type" );
            query.setFilter( "this.type == type" );
            pm.getFetchPlan().addGroup( BUILD_TEMPLATE_BUILD_DEFINITIONS );
            List result = (List) query.execute( type );
            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public BuildDefinitionTemplate getContinuumBuildDefinitionTemplateWithType( String type )
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.declareImports( "import java.lang.String" );
            query.declareParameters( "String type" );
            query.setFilter( "continuumDefault == true && this.type == type" );
            pm.getFetchPlan().addGroup( BUILD_TEMPLATE_BUILD_DEFINITIONS );
            List result = (List) query.execute( type );
            if ( result == null || result.isEmpty() )
            {
                return null;
            }
            return (BuildDefinitionTemplate) pm.detachCopy( result.get( 0 ) );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }

    public List<BuildDefinitionTemplate> getContinuumDefaultdDefinitions()
        throws ContinuumStoreException
    {
        PersistenceManager pm = getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( BuildDefinitionTemplate.class, true );

            Query query = pm.newQuery( extent );
            query.setFilter( "continuumDefault == true" );

            List result = (List) query.execute();

            return result == null ? Collections.EMPTY_LIST : (List) pm.detachCopyAll( result );
        }
        finally
        {
            tx.commit();

            rollback( tx );
        }
    }
}
