/**
 * 
 */
package org.apache.maven.continuum.store.matcher;

import org.apache.maven.continuum.model.CommonUpdatableEntity;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.Query;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.springframework.test.annotation.NotTransactional;

/**
 * {@link Matcher} extension that provides a semantic convenience to assert if a persistable JPA entity was removed from
 * the underlying store.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaEntity<T extends CommonUpdatableEntity, Q extends Query<T>> extends BaseMatcher<T>
{

    /**
     * Entity to check
     */
    private T entity;

    private Class<T> klass;

    private Store<T, Q> store;

    public JpaEntity( Store<T, Q> s, Class<T> c, T t )
    {
        this.store = s;
        this.klass = c;
        this.entity = t;
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.hamcrest.Matcher#matches(java.lang.Object)
     */
    @NotTransactional
    public boolean matches( Object o )
    {
        Assert.assertNotNull( store );
        try
        {
            T obj = store.lookup( klass, entity.getId() );
            return ( null == obj );
        }
        catch ( EntityNotFoundException e )
        {
            return true;
        }
        catch ( StoreException e )
        {
            // TODO: How do we handle this?
            return false;
        }
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
     */
    public void describeTo( Description description )
    {
        description.appendText( "Entity is deleted" );
    }

    @Factory
    public static <T extends CommonUpdatableEntity, Q extends Query<T>> Matcher<T> isDeleted( Store<T, Q> s,
                                                                                              Class<T> c, T t )
    {
        JpaEntity<T, Q> matcher = new JpaEntity<T, Q>( s, c, t );
        return matcher;
    }

}
