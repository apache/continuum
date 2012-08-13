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

import java.io.File;
import java.io.IOException;

/**
 * Delegate to the correct data management tool.
 *
 * @version $Id$
 */
public interface DataManagementTool
{
    /**
     * Backup the database.
     *
     * @param backupDirectory the directory to backup to
     * @throws java.io.IOException     if there is a problem writing to the backup file
     * @throws DataManagementException if there is a problem reading from the database
     */
    void backupDatabase( File backupDirectory )
        throws IOException, DataManagementException;

    /**
     * Restore the database.
     *
     * @param backupDirectory the directory where the backup to restore from resides
     * @param strict
     * @throws java.io.IOException     if there is a problem reading the backup file
     * @throws DataManagementException if there is a problem parsing the backup file
     */
    void restoreDatabase( File backupDirectory, boolean strict )
        throws IOException, DataManagementException;

    /**
     * Smoke the database.
     */
    void eraseDatabase();
}
