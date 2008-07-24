package org.apache.maven.continuum.management;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Enumeration of known database formats.
 */
public enum DatabaseFormat
{
    /**
     * Continuum 1.0.3 build database.
     *
     * @todo this hasn't been completed/tested - the model needs to be annotated with 1.0.3 metadata and converters written.
     */
    CONTINUUM_103( "1.1.1", "legacy-continuum-jdo" )
        {
            public boolean isConvertibleFrom( DatabaseFormat sourceFormat )
            {
                return false;
            }
        },

    /**
     * Continuum pre-alpha build database. Plexus Security 1.0-alpha-5.
     */
    CONTINUUM_109( "1.1.1", "legacy-continuum-jdo", "legacy-redback-jdo" )
        {
            public boolean isConvertibleFrom( DatabaseFormat sourceFormat )
            {
                return false;
            }
        },

    /**
     * Continuum 1.1+ build database.
     */
    CONTINUUM_11( "1.1.6", "continuum-jdo", "redback-jdo" )
        {
            public boolean isConvertibleFrom( DatabaseFormat sourceFormat )
            {
                return sourceFormat == CONTINUUM_103 || sourceFormat == CONTINUUM_109;
            }
        };

    private final String jpoxVersion;

    private final String continuumToolRoleHint;

    private final String redbackToolRoleHint;

    DatabaseFormat( String jpoxVersion, String continuumToolRoleHint )
    {
        this.jpoxVersion = jpoxVersion;

        this.continuumToolRoleHint = continuumToolRoleHint;

        this.redbackToolRoleHint = null;
    }

    DatabaseFormat( String jpoxVersion, String continuumToolRoleHint, String redbackToolRoleHint )
    {
        this.jpoxVersion = jpoxVersion;

        this.continuumToolRoleHint = continuumToolRoleHint;

        this.redbackToolRoleHint = redbackToolRoleHint;
    }

    /**
     * Whether a database can be converted from the given format to this format.
     *
     * @param sourceFormat the database format to convert from
     * @return whether it can be successfully converted from that format
     */
    public abstract boolean isConvertibleFrom( DatabaseFormat sourceFormat );

    public String getJpoxVersion()
    {
        return jpoxVersion;
    }

    public String getContinuumToolRoleHint()
    {
        return continuumToolRoleHint;
    }

    public String getRedbackToolRoleHint()
    {
        return redbackToolRoleHint;
    }
}
