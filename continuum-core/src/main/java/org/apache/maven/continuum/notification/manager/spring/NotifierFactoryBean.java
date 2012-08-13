package org.apache.maven.continuum.notification.manager.spring;

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

import org.apache.maven.continuum.notification.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class NotifierFactoryBean
    implements FactoryBean, ApplicationContextAware
{
    private static final Logger log = LoggerFactory.getLogger( NotifierFactoryBean.class );

    private ApplicationContext applicationContext;

    public Object getObject()
        throws Exception
    {
        Map<String, Notifier> notifiers = new HashMap<String, Notifier>();

        Map<String, Notifier> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors( applicationContext,
                                                                                      Notifier.class );

        for ( Notifier notifier : beans.values() )
        {

            if ( notifiers.containsKey( notifier.getType() ) )
            {
                throw new BeanInitializationException(
                    "There are two Notifier beans in the appllication context for Notifier type " + notifier.getType() +
                        ". Probably two conflicting scm implementations are present in the classpath." );
            }

            if ( log.isDebugEnabled() )
            {
                log.debug(
                    "put provider with type " + notifier.getType() + " and class " + notifier.getClass().getName() );
            }
            notifiers.put( notifier.getType(), notifier );
        }
        return notifiers;
    }

    public Class getObjectType()
    {
        return Map.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void setApplicationContext( ApplicationContext applicationContext )
        throws BeansException
    {
        this.applicationContext = applicationContext;
    }
}
