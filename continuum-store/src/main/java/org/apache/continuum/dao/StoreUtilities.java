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

import org.codehaus.plexus.jdo.JdoFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jdo.PersistenceManagerFactory;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.continuum.dao.StoreUtilities"
 */
@Service( "storeUtilities" )
public class StoreUtilities
{
    /**
     * @plexus.requirement role-hint="continuum"
     */
    @Resource( name = "jdoFactory#continuum" )
    private JdoFactory continuumJdoFactory;

    private PersistenceManagerFactory continuumPersistenceManagerFactory;

    public PersistenceManagerFactory getContinuumPersistenceManagerFactory()
    {
        if ( continuumPersistenceManagerFactory == null )
        {
            continuumPersistenceManagerFactory = continuumJdoFactory.getPersistenceManagerFactory();
        }
        return continuumPersistenceManagerFactory;
    }
}
