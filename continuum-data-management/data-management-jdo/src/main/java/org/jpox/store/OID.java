/**********************************************************************
 Copyright (c) 2002 Kelly Grizzle (TJDO) and others. All rights reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.


 Contributors:
 2003 Erik Bengtson - Refactored OID
 2003 Andy Jefferson - fixed OID(String)
 2003 Andy Jefferson - coding standards
 2004 Andy Jefferson - fixes to allow full use of long or String OIDs
 2005 Erik Bengtson - removed oidType
 2007 Brett Porter - changed hashcode algorithm to avoid collisions (see CORE-3297)
 ...
 **********************************************************************/
package org.jpox.store;

import org.jpox.ClassNameConstants;
import org.jpox.util.Localiser;

/**
 * An object identifier. OIDs are normally used as object identifiers for
 * persistent objects that use datastore identity. They're also used for
 * view objects, which actually use non-datastore identity. The behaviour of
 * this class is governed by JDO spec 5.4.3.
 *
 * @version $Revision: 1.17 $
 */
public class OID
    implements java.io.Serializable
{
    private transient static final Localiser LOCALISER = Localiser.getInstance( "org.jpox.store.Localisation" );

    /**
     * Separator to use between fields.
     */
    private transient static final String oidSeparator = "[OID]";

    // JDO spec 5.4.3 - serializable fields required to be public.

    /**
     * The identity.
     */
    public final Object oid;

    /**
     * The PersistenceCapable class name
     */
    public final String pcClass;

    /**
     * pre-created toString to improve performance *
     */
    public final String toString;

    /**
     * pre-created hasCode to improve performance *
     */
    public final int hashCode;

    /**
     * Creates an OID with the no value.
     * Required by the JDO spec
     */
    public OID()
    {
        oid = null;
        pcClass = null;
        toString = null;
        hashCode = -1;
    }

    /**
     * Create a string datastore identity
     *
     * @param pcClass The PersistenceCapable class that this represents
     * @param object  The value
     */
    public OID( String pcClass, Object object )
    {
        this.pcClass = pcClass;
        this.oid = object;

        StringBuffer s = new StringBuffer();
        s.append( this.oid.toString() );
        s.append( oidSeparator );
        s.append( this.pcClass );
        toString = s.toString();
        hashCode = this.oid.hashCode() * 39 + pcClass.hashCode();
    }

    /**
     * Constructs an OID from its string representation that is
     * consistent with the output of toString().
     *
     * @param str the string representation of an OID.
     * @throws IllegalArgumentException if the given string representation is not valid.
     * @see #toString
     */
    public OID( String str )
        throws IllegalArgumentException
    {
        if ( str.length() < 2 )
        {
            throw new IllegalArgumentException( LOCALISER.msg( "OID.InvalidValue", str ) );
        }

        int start = 0;
        int end = str.indexOf( oidSeparator, start );
        String oidStr = str.substring( start, end );
        Object oidValue = null;
        try
        {
            // Use Long if possible, else String
            oidValue = new Long( oidStr );
        }
        catch ( NumberFormatException nfe )
        {
            oidValue = oidStr;
        }
        oid = oidValue;

        start = end + oidSeparator.length();
        this.pcClass = str.substring( start, str.length() );

        toString = str;
        hashCode = this.oid.hashCode() * 39 + pcClass.hashCode();
    }

    /**
     * Returns copy of the requested oid to be accessed by the user.
     *
     * @return Copy of the OID.
     */
    public Object getNewObjectIdCopy()
    {
        return new OID( this.pcClass, this.oid );
    }

    /**
     * Provides the OID in a form that can be used by the database as a key.
     *
     * @return The key value
     */
    public Object keyValue()
    {
        return oid;
    }

    /**
     * Equality operator.
     *
     * @param obj Object to compare against
     * @return Whether they are equal
     */
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( obj == this )
        {
            return true;
        }
        if ( !( obj.getClass().getName().equals( ClassNameConstants.OID ) ) )
        {
            return false;
        }
        if ( hashCode() != obj.hashCode() )
        {
            return false;
        }
        return true;
    }

    /**
     * Accessor for the hashcode
     *
     * @return Hashcode for this object
     */
    public int hashCode()
    {
        return hashCode;
    }

    /**
     * Returns the string representation of the OID.
     * Will be a string such as "1[OID]org.jpox.samples.MyClass"
     * where
     * <UL>
     * <LI>1 is the identity "id"</LI>
     * <LI>class name is the name of the PersistenceCapable class that it represents</LI>
     * </UL>
     *
     * @return the string representation of the OID.
     */
    public String toString()
    {
        return toString;
    }

    /**
     * Accessor for the PC class name
     *
     * @return the PC Class
     */
    public String getPcClass()
    {
        return pcClass;
    }
}