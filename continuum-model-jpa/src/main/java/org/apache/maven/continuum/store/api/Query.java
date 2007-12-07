/**
 * 
 */
package org.apache.maven.continuum.store.api;

import java.util.Map;

/**
 * Wraps up Type Query criteria to be used by store extensions to filter (and obtain) matching type instances.
 * <p>
 * Implementations/extensions are expected to override {@link Object#toString()} method and return a <b>JPQL</b>
 * formatted string. The JPQL string is consumed by the {@link Store} implementation in {@link Store#query(Query)}
 * operations.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public interface Query<Q>
{

    /**
     * Returns this instance of {@link Query} as a JPQL String.
     * 
     * @param whereClause
     *            {@link Map} containing the named parameters to be substituted in the JPQL query. This is populated by
     *            the {@link Query} implementation and subsequently used by the {@link Store} implementation to
     *            interpolate the parameters before the JPQL query is executed.
     * 
     * @return {@link Query} as a JPQL String
     */
    public String toString( Map<String, Object> whereClause );
}
