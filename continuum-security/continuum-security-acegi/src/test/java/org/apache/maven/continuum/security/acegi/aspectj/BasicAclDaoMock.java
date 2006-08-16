package org.apache.maven.continuum.security.acegi.aspectj;

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

import org.acegisecurity.acl.basic.AclObjectIdentity;
import org.acegisecurity.acl.basic.BasicAclDao;
import org.acegisecurity.acl.basic.BasicAclEntry;
import org.acegisecurity.acl.basic.NamedEntityObjectIdentity;
import org.acegisecurity.acl.basic.SimpleAclEntry;
import org.apache.maven.continuum.model.project.Project;

/**
 * {@link BasicAclDao} that will allow READ for {@link Project} with id 1.
 * 
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class BasicAclDaoMock
    implements BasicAclDao
{
    private SimpleAclEntry aclEntry1, aclEntryDefault;

    public BasicAclDaoMock( )
    {
        aclEntryDefault = new SimpleAclEntry();
        aclEntryDefault.addPermission( SimpleAclEntry.NOTHING );
        aclEntryDefault.setRecipient( AbstractContinuumSecurityAspectTest.USERNAME );

        aclEntry1 = new SimpleAclEntry();
        aclEntry1.addPermission( SimpleAclEntry.READ );
        aclEntry1.setRecipient( AbstractContinuumSecurityAspectTest.USERNAME );
    }

    public BasicAclEntry[] getAcls( AclObjectIdentity aclObjectIdentity )
    {
        NamedEntityObjectIdentity objectIdentity = ( (NamedEntityObjectIdentity) aclObjectIdentity );
        
        if ( objectIdentity.getId().equals( "1" ) )
        {
            return new BasicAclEntry[] { aclEntry1 };
        }
        else
        {
            return new BasicAclEntry[] { aclEntryDefault };
        }
    }
}
