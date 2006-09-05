package org.apache.maven.continuum.model.system;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.user.model.User;

/**
 * @deprecated user {@link User} instead
 * 
 * @author Henry Isidro
 * @version $Id$
 */
public class ContinuumUser
    extends User
{
    /*
    public String getPassword()
    {
        return super.getPassword();
    }

    public void setPassword( String password )
    {
        setEncodedPassword( crypt( password ) );
    }
    */
    
    public boolean equalsPassword( String password )
    {
        if ( getEncodedPassword() == null && password == null )
        {
            return true;
        }

        if ( ( getEncodedPassword() == null && password != null ) || ( getEncodedPassword() == null && password != null ) )
        {
            return false;
        }

        return getEncodedPassword().equals( crypt( password ) );
    }

    private String crypt( String data )
    {
        try
        {
            java.security.MessageDigest digester = java.security.MessageDigest.getInstance( "SHA-1" );
            digester.reset();
            digester.update( data.getBytes() );
            return encode( digester.digest() );
        }
        catch( Exception e )
        {
            return data;
        }
    }

    /**
     * Encodes a 128 bit or 160-bit byte array into a String.
     *
     * @param binaryData Array containing the digest
     * @return Encoded hex string, or null if encoding failed
     */
    
    private String encode( byte[] binaryData )
    {
        if ( binaryData.length != 16 && binaryData.length != 20 )
        {
            int bitLength = binaryData.length * 8;
            throw new IllegalArgumentException( "Unrecognised length for binary data: " + bitLength + " bits" );
        }

        String retValue = "";

        for ( int i = 0; i < binaryData.length; i++ )
        {
            String t = Integer.toHexString( binaryData[i] & 0xff );

            if ( t.length() == 1 )
            {
                retValue += ( "0" + t );
            }
            else
            {
                retValue += t;
            }
        }

        return retValue.trim();
    }
        
}
