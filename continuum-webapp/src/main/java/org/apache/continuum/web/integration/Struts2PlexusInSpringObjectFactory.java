package org.apache.continuum.web.integration;

/*
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
 */

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.validator.Validator;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.spring.StrutsSpringObjectFactory;
import org.codehaus.plexus.spring.PlexusToSpringUtils;

import javax.servlet.ServletContext;
import java.util.Map;

public class Struts2PlexusInSpringObjectFactory
        extends StrutsSpringObjectFactory
{
    @Inject
    public Struts2PlexusInSpringObjectFactory(
            @Inject(value=StrutsConstants.STRUTS_OBJECTFACTORY_SPRING_AUTOWIRE,required=false) String autoWire,
            @Inject(value=StrutsConstants.STRUTS_OBJECTFACTORY_SPRING_AUTOWIRE_ALWAYS_RESPECT,required=false) String alwaysAutoWire,
            @Inject(value=StrutsConstants.STRUTS_OBJECTFACTORY_SPRING_USE_CLASS_CACHE,required=false) String useClassCacheStr,
            @Inject(value=StrutsConstants.STRUTS_OBJECTFACTORY_SPRING_ENABLE_AOP_SUPPORT,required=false) String enableAopSupport,
            @Inject ServletContext servletContext,
            @Inject(StrutsConstants.STRUTS_DEVMODE) String devMode,
            @Inject Container container)
    {
        super(autoWire, alwaysAutoWire, useClassCacheStr, enableAopSupport, servletContext, devMode, container);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.opensymphony.xwork2.spring.SpringObjectFactory#buildBean(java.lang.String, java.util.Map)
     */
    @Override
    public Object buildBean( String name, Map map )
            throws Exception
    {
        String id = PlexusToSpringUtils.buildSpringId(Action.class, name);
        if ( appContext.containsBean( id ) )
        {
            return super.buildBean( id, map );
        }

        id = PlexusToSpringUtils.buildSpringId( Result.class, name );
        if ( appContext.containsBean( id ) )
        {
            return super.buildBean( id, map );
        }

        id = PlexusToSpringUtils.buildSpringId( Interceptor.class, name );
        if ( appContext.containsBean( id ) )
        {
            return super.buildBean( id, map );
        }

        id = PlexusToSpringUtils.buildSpringId( Validator.class, name );
        if ( appContext.containsBean( id ) )
        {
            return super.buildBean( id, map );
        }
        return super.buildBean( name, map );
    }

    public Validator buildValidator( String className, Map params, Map extraContext )
            throws Exception
    {
        String id = PlexusToSpringUtils.buildSpringId( Validator.class, className );
        if ( appContext.containsBean( id ) )
        {
            return (Validator) appContext.getBean( id );
        }
        return super.buildValidator( className, params, extraContext );
    }

    @Override
    public Class getClassInstance( String className )
            throws ClassNotFoundException
    {
        String id = PlexusToSpringUtils.buildSpringId( Action.class, className );
        if ( appContext.containsBean( id ) )
        {
            return appContext.getType( id );
        }

        id = PlexusToSpringUtils.buildSpringId( Result.class, className );
        if ( appContext.containsBean( id ) )
        {
            return appContext.getType( id );
        }

        id = PlexusToSpringUtils.buildSpringId( Interceptor.class, className );
        if ( appContext.containsBean( id ) )
        {
            return appContext.getType( id );
        }
        id = PlexusToSpringUtils.buildSpringId( Validator.class, className );
        if ( appContext.containsBean( id ) )
        {
            return appContext.getType( id );
        }
        return super.getClassInstance( className );
    }
}
