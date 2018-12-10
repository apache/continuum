/**
 * 
 */
package org.apache.maven.continuum.store.api;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class QueryFactory
{
    public static <T, Q extends Query<T>> Q createQuery( Class<Q> klass )
    {
        Q qry = null;
        try
        {
            qry = klass.newInstance();
        }
        catch ( InstantiationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IllegalAccessException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return qry;
    }
}
