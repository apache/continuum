package org.codehaus.continuum.web.tool;

import ognl.Ognl;
import ognl.OgnlException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.formica.Element;
import org.codehaus.plexus.formica.Form;
import org.codehaus.plexus.formica.FormManager;
import org.codehaus.plexus.formica.FormNotFoundException;
import org.codehaus.plexus.formica.Operation;
import org.codehaus.plexus.formica.SummaryElement;
import org.codehaus.plexus.formica.web.ContentGenerator;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: CssTool.java,v 1.2 2005/04/06 14:10:38 trygvis Exp $
 */
public class FormicaTool
    extends AbstractLogEnabled
    implements Contextualizable
{
    private FormManager formManager;

    private PlexusContainer container;

    // ----------------------------------------------------------------------
    // Form
    // ----------------------------------------------------------------------

    public Form getForm( String fid )
        throws FormToolException
    {
        try
        {
            return formManager.getForm( fid );
        }
        catch ( FormNotFoundException e )
        {
            throw new FormToolException( "Cannot find form with id = " + fid, e );
        }
    }

    // ----------------------------------------------------------------------
    // Summary
    // ----------------------------------------------------------------------

    public Object getSummaryItems( String formId )
        throws FormToolException
    {
        Form form = null;

        Object component = null;

        try
        {
            form = formManager.getForm( formId );

            component = container.lookup( assertNotEmpty( form, form.getSourceRole(), "source role" ) );
        }
        catch ( FormNotFoundException e )
        {
            throw new FormToolException( "Cannot find form with id = " + formId, e );
        }
        catch ( ComponentLookupException e )
        {
            throw new FormToolException( "Cannot find component with role = " + form.getSourceRole(), e );
        }

        String expr = form.getSummaryCollectionExpression();

        return getValue( expr, Collections.EMPTY_MAP, component );
    }

    public String getSummaryItem( Form form, SummaryElement se, Object item )
        throws FormToolException
    {
        return getItem( form, form.getElement( se.getId() ), item );
    }

    public String getItem( Form form, Element element, String id )
        throws FormToolException
    {
        if ( id == null )
        {
            return null;
        }

        return getItem( form, element, getItem( form, id ) );
    }


    public String getItem( Form form, Element element, Object item )
        throws FormToolException
    {
        try
        {
            String text;

            if ( element.getContentGenerator() != null )
            {
                ContentGenerator cg = (ContentGenerator) container.lookup( ContentGenerator.ROLE, element.getContentGenerator() );

                text = cg.generate( item );
            }
            else
            {
                Object value =  Ognl.getValue( element.getExpression(), item );

                text = value.toString();

            }

            return text;
        }
        catch( ComponentLookupException e )
        {
            throw new FormToolException( "Content generator with id = " + element.getContentGenerator() + " does not exist", e );
        }
        catch ( OgnlException e )
        {
            throw new FormToolException( "Error extracting value from " + item + " using the expression " + element.getExpression(), e );
        }
    }

    public String getAUElement( Form form, Element element, Object data, RunData rundata )
        throws FormToolException
    {
        if ( data == null )
        {
            return null;
        }

        Object o = rundata.getParameters().getString( element.getId() );

        if ( o == null )
        {
            try
            {
                o = Ognl.getValue( element.getExpression(), data );
            }
            catch ( OgnlException e )
            {
                throw new FormToolException( "Error evaluating expression: ", e );
            }
        }

        return o.toString();
    }


    public String getOperationUrl( Form form, Object item, Operation op )
        throws FormToolException
    {
        String id = null;

        String type = null;

        // TODO; throw an exception if the expression key isn't there
        try
        {
            id = (String) Ognl.getValue( form.getKeyExpression(), item );

            type = (String) Ognl.getValue( form.getTypeExpression(), item );
        }
        catch ( OgnlException e )
        {
            throw new FormToolException( "Error retrieving expression:", e );
        }

        String s = StringUtils.replace( op.getAction(), "$id$", id );

        s = StringUtils.replace( s, "$formId$", form.getId() );

        s = StringUtils.replace( s, "$type$", type );

        return s;
    }

    // ----------------------------------------------------------------------
    // View
    // ----------------------------------------------------------------------

    public Object getItem( Form form, String id )
        throws FormToolException
    {
        Object component;

        try
        {
            component = container.lookup( assertNotEmpty( form, form.getSourceRole(), "source role" ) );
        }
        catch ( ComponentLookupException e )
        {
            throw new FormToolException( "Cannot lookup source component: ", e );
        }

        Map map = new HashMap();

        map.put( "id", id );

        String expr = assertNotEmpty( form, form.getLookupExpression(), "lookup expression" );

        return getValue( expr, map, component );
    }

    // ----------------------------------------------------------------------
    // Utilities
    // ----------------------------------------------------------------------

    private String assertNotEmpty( Form form, String value, String field )
        throws FormToolException
    {
        if ( StringUtils.isEmpty( value ) )
        {
            throw new FormToolException( "Missing " + field + " from form '" + form.getId() + "'." );
        }

        return value;
    }

    private Object getValue( String expr, Map map, Object o )
        throws FormToolException
    {
        Object data;

        try
        {
            data = Ognl.getValue( expr, map, o );
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error while evaluation OGNL expression '" + expr + "'.", e );

            throw new FormToolException( "Error while evaluation OGNL expression '" + expr + "'.", e );
        }

        return data;
    }

    // ----------------------------------------------------------------------
    // Lifecylce Management
    // ----------------------------------------------------------------------

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
