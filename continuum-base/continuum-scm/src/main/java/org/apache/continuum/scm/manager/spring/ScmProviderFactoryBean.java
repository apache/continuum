package org.apache.continuum.scm.manager.spring;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.provider.ScmProvider;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>
 * Factory bean to inject all beans of type {@link ScmProvider}
 * </p>
 * <p>
 * <code>&lt;bean id="scmProviders" class="org.apache.continuum.scm.manager.spring.ScmManagerFactoryBean"/>
 </code>
 * </p>
 * 
 * @author Carlos Sanchez <carlos@apache.org>
 */
public class ScmProviderFactoryBean
    implements FactoryBean, ApplicationContextAware
{
    private ApplicationContext applicationContext;
   
    /**
     * FIXME : change how we find scm implementations 
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject()
        throws Exception
    {
        Map<String, ScmProvider> providers = new HashMap<String, ScmProvider>();
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
        providers =  PlexusToSpringUtils.lookupMap(PlexusToSpringUtils.buildSpringId( ScmProvider.class ), applicationContext );
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
