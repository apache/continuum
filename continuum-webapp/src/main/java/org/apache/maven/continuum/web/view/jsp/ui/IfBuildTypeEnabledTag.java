package org.apache.maven.continuum.web.view.jsp.ui;

import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

/**
 * ifBuildTypeEnabledTag:
 *
 * @author Jan Ancajas <jansquared@gmail.com>
 * @version $Id: IfBuildTypeEnabledTag.java
 */
public class IfBuildTypeEnabledTag
    extends ConditionalTagSupport
{
    private Continuum continuum;

    private String buildType;

    public static final String DISTRIBUTED = "distributed";

    protected boolean condition()
        throws JspTagException
    {

        ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(
            pageContext.getServletContext() );
        this.setContinuum( (Continuum) applicationContext.getBean( PlexusToSpringUtils.buildSpringId( Continuum.ROLE,
                                                                                                      "default" ) ) );

        if ( continuum == null )
        {
            throw new JspTagException( "cannot lookup component:  " + Continuum.ROLE );
        }

        if ( DISTRIBUTED.equals( buildType ) )
        {
            return continuum.getConfiguration().isDistributedBuildEnabled();
        }

        // left out 'parallel' buildType checking for cyclomatic complexity's sake :)
        return !continuum.getConfiguration().isDistributedBuildEnabled();

    }

    public String getBuildType()
    {
        return buildType;
    }

    public void setBuildType( String buildType )
    {
        this.buildType = buildType;
    }

    public Continuum getContinuum()
    {
        return continuum;
    }

    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
    }
}
