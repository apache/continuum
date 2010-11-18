package org.apache.continuum.xmlrpc.server;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.xmlrpc.server.ContinuumServiceImpl;

public class ContinuumServiceImplStub
    extends ContinuumServiceImpl
{
    protected void checkBuildProjectInGroupAuthorization( String resource )
        throws ContinuumException
    {
        // do nothing
    }
}
