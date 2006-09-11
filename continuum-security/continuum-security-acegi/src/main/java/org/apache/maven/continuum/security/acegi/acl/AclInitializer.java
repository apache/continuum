package org.apache.maven.continuum.security.acegi.acl;

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

import org.acegisecurity.acl.basic.NamedEntityObjectIdentity;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.user.acegi.acl.basic.ExtendedSimpleAclEntry;

/**
 * Initialize the ACL system with a parent ACL for all {@link ProjectGroup}s.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AclInitializer
    extends org.apache.maven.user.acegi.acl.AclInitializer
{
    public static final int PARENT_PROJECT_GROUP_ACL_ID = 0;

    protected void insertDefaultData()
    {
        /* admin can do anything with project number 1 */
        ExtendedSimpleAclEntry aclEntry = new ExtendedSimpleAclEntry();
        aclEntry.setAclObjectIdentity( new NamedEntityObjectIdentity( ProjectGroup.class.getName(), Integer
            .toString( PARENT_PROJECT_GROUP_ACL_ID ) ) );
        aclEntry.setRecipient( "ROLE_admin" );
        aclEntry.addPermission( ExtendedSimpleAclEntry.ADMINISTRATION );
        getDao().create( aclEntry );
    }
}
