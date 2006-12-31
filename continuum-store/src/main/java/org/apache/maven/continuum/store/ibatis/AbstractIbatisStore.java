package org.apache.maven.continuum.store.ibatis;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since
 */
public abstract class AbstractIbatisStore extends AbstractLogEnabled implements Initializable
{

    /**
     * Acts as a client to initiate operations on the underlying data store.
     */
    private SqlMapClient sqlMapClient;

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        // TODO: initialize SQL Map client.
    }

    /**
     * @return the sqlMapClient
     */
    public SqlMapClient getSqlMapClient()
    {
        return sqlMapClient;
    }

}
