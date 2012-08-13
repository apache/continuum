package org.apache.continuum.scm.manager.spring;

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

import org.apache.maven.scm.provider.ScmProvider;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * <p>
 * Factory bean to inject all beans of type {@link ScmProvider}
 * </p>
 * <p>
 * <code>&lt;bean id="scmProviders" class="org.apache.continuum.scm.manager.spring.ScmManagerFactoryBean"/>
 * </code>
 * </p>
 *
 * @author Carlos Sanchez <carlos@apache.org>
 * @version $Id$
 */
public class ScmProviderFactoryBean
    implements FactoryBean, ApplicationContextAware
{
    private ApplicationContext applicationContext;

    /**
     * FIXME : change how we find scm implementations
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject()
        throws Exception
    {
        Map<String, ScmProvider> providers;
        /*
         olamy : comment the pure spring use because we have a duplicate between cvs java and cvs native
          
         Map<String, ScmProvider> beans =
            BeanFactoryUtils.beansOfTypeIncludingAncestors( applicationContext, ScmProvider.class );
        
       
        for ( ScmProvider provider : beans.values() )
        {
            
            if ( providers.containsKey( provider.getScmType() ) )
            {
                throw new BeanInitializationException(
                                                       "There are to ScmProvider beans in the appllication context for Scm type " +
                                                           provider.getScmType() +
                                                           ". Probably two conflicting scm implementations are present in the classpath." );
            }
            
            if (log.isDebugEnabled())
            {
                log.debug( "put provider with type " + provider.getScmType() + " and class " + provider.getClass().getName() );
            }
            providers.put( provider.getScmType(), provider );
        }*/
        providers = PlexusToSpringUtils.lookupMap( PlexusToSpringUtils.buildSpringId( ScmProvider.class ),
                                                   applicationContext );
        return providers;
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
