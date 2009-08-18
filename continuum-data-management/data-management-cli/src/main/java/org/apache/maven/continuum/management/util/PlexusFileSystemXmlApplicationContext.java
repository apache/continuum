package org.apache.maven.continuum.management.util;

import java.io.IOException;

import org.codehaus.plexus.spring.PlexusXmlBeanDefinitionReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class PlexusFileSystemXmlApplicationContext
    extends FileSystemXmlApplicationContext
{
    private static PlexusApplicationContextDelegate delegate = new PlexusApplicationContextDelegate();

    public PlexusFileSystemXmlApplicationContext( String configLocation )
    {
        super( configLocation );
    }

    public PlexusFileSystemXmlApplicationContext( String[] configLocations )
    {
        super( configLocations );
    }

    public PlexusFileSystemXmlApplicationContext( String[] configLocations, ApplicationContext parent )
    {
        super( configLocations, parent );
    }

    public PlexusFileSystemXmlApplicationContext( String[] configLocations, boolean refresh )
    {
        super( configLocations, refresh );
    }

    public PlexusFileSystemXmlApplicationContext( String[] configLocations, boolean refresh, ApplicationContext parent )
    {
        super( configLocations, refresh, parent );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.context.support.AbstractXmlApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
     */
    protected void loadBeanDefinitions( XmlBeanDefinitionReader reader )
        throws BeansException, IOException
    {
        delegate.loadBeanDefinitions( reader );
        super.loadBeanDefinitions( reader );
    }

    /**
     * Copied from {@link AbstractXmlApplicationContext}
     * Loads the bean definitions via an XmlBeanDefinitionReader.
     * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
     * @see #initBeanDefinitionReader
     * @see #loadBeanDefinitions
     */
    protected void loadBeanDefinitions( DefaultListableBeanFactory beanFactory ) throws IOException {
        // Create a new XmlBeanDefinitionReader for the given BeanFactory.
        XmlBeanDefinitionReader beanDefinitionReader = new PlexusXmlBeanDefinitionReader( beanFactory );

        // Configure the bean definition reader with this context's
        // resource loading environment.
        beanDefinitionReader.setResourceLoader( this );
        beanDefinitionReader.setEntityResolver( new ResourceEntityResolver( this ) );

        // Allow a subclass to provide custom initialization of the reader,
        // then proceed with actually loading the bean definitions.
        initBeanDefinitionReader( beanDefinitionReader );
        loadBeanDefinitions( beanDefinitionReader );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.context.support.AbstractApplicationContext#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    protected void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory )
    {
        delegate.postProcessBeanFactory( beanFactory, this );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.context.support.AbstractApplicationContext#doClose()
     */
    protected void doClose()
    {
        delegate.doClose();
        super.doClose();
    }
}