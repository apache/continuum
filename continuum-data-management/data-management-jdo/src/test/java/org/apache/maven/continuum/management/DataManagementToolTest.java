package org.apache.maven.continuum.management;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.maven.continuum.store.AbstractContinuumStoreTestCase;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.custommonkey.xmlunit.Diff;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.stream.XMLStreamException;

/**
 * Test the database management tool.
 */
public class DataManagementToolTest
    extends AbstractContinuumStoreTestCase
{
    private DataManagementTool dataManagementTool;

    private File targetDirectory;

    private static final String BUILDS_XML = "builds.xml";

    protected void setUp()
        throws Exception
    {
        super.setUp();

        dataManagementTool = (DataManagementTool) lookup( DataManagementTool.class.getName(), "continuum-jdo" );

        targetDirectory = createBackupDirectory();
    }

/*
    protected ContinuumStore createStore()
        throws Exception
    {
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE );

        File database = getTestFile( "target/database/" + getName());
        FileUtils.deleteDirectory( database );

        jdoFactory.setDriverName( "org.apache.derby.jdbc.EmbeddedDriver");
        jdoFactory.setUrl( "jdbc:derby:"+database.getAbsolutePath() + ";create=true" );

        return (ContinuumStore) lookup( ContinuumStore.ROLE );
    }
*/

    public void testBackupBuilds()
        throws IOException, ContinuumStoreException, XMLStreamException, Exception
    {
        createBuildDatabase( true );

        // test sanity check
        assertBuildDatabase();

        dataManagementTool.backupDatabase( targetDirectory );

        File backupFile = new File( targetDirectory, BUILDS_XML );

        assertTrue( "Check database exists", backupFile.exists() );

        StringWriter sw = new StringWriter();

        IOUtil.copy( getClass().getResourceAsStream( "/expected.xml" ), sw );

        //assertEquals( "Check database content", removeTimestampVariance( sw.toString() ),
        //              removeTimestampVariance( FileUtils.fileRead( backupFile ) ) );        
        assertXmlSimilar( removeTimestampVariance( sw.toString() ), removeTimestampVariance( FileUtils.fileRead(
            backupFile ) ) );
    }

    public void testEraseBuilds()
        throws Exception
    {
        createBuildDatabase( false );

        dataManagementTool.eraseDatabase();

        assertEmpty( false );
    }

    public void testRestoreBuilds()
        throws Exception
    {
        createBuildDatabase( false, true );

        assertEmpty( true );

        File backupFile = new File( targetDirectory, BUILDS_XML );

        IOUtil.copy( getClass().getResourceAsStream( "/expected.xml" ), new FileWriter( backupFile ) );

        dataManagementTool.restoreDatabase( targetDirectory, true );
/*
        // TODO: why is this wrong?
        assertBuildDatabase();

        // Test that it worked. Relies on BackupBuilds having worked
        dataManagementTool.backupDatabase( targetDirectory );

        StringWriter sw = new StringWriter();

        IOUtil.copy( getClass().getResourceAsStream( "/expected.xml" ), sw );

        //assertEquals( "Check database content", removeTimestampVariance( sw.toString() ),
        //              removeTimestampVariance( FileUtils.fileRead( backupFile ) ) );
        assertXmlSimilar( removeTimestampVariance( sw.toString() ), removeTimestampVariance( FileUtils
            .fileRead( backupFile ) ) );*/
    }

    private static File createBackupDirectory()
    {
        String timestamp = new SimpleDateFormat( "yyyyMMdd.HHmmss", Locale.US ).format( new Date() );

        File targetDirectory = getTestFile( "target/backups/" + timestamp );
        targetDirectory.mkdirs();

        return targetDirectory;
    }

    private static String removeTimestampVariance( String content )
    {
        /*return fixXmlQuotes( removeTagContent( removeTagContent( removeTagContent( removeTagContent( content,
                                                                                                     "startTime" ),
                                                                                   "endTime" ), "date" ), "id" ) );*/

        return removeTagContent( removeTagContent( removeTagContent( removeTagContent( content, "startTime" ),
                                                                     "endTime" ), "date" ), "id" );
    }

    public void assertXmlIdentical( String expected, String test )
        throws Exception
    {
        String expectedXml = prettyXmlPrint( expected );
        String testXml = prettyXmlPrint( test );
        Diff diff = new Diff( expectedXml, testXml );
        StringBuffer diffMessage = new StringBuffer();
        assertTrue( " xml diff not identical " + diff.appendMessage( diffMessage ).toString(), diff.identical() );
    }


    public void assertXmlSimilar( String expected, String test )
        throws Exception
    {
        String expectedXml = prettyXmlPrint( expected );
        String testXml = prettyXmlPrint( test );
        Diff diff = new Diff( expectedXml, testXml );
        StringBuffer diffMessage = new StringBuffer();
        assertTrue( " xml diff not similar " + diff.appendMessage( diffMessage ).toString(), diff.similar() );
    }

    public String prettyXmlPrint( String xml )
        throws Exception
    {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build( new StringReader( xml ) );
        XMLOutputter outp = new XMLOutputter();

        outp.setFormat( Format.getPrettyFormat() );

        StringWriter sw = new StringWriter();

        outp.output( document, sw );
        return sw.getBuffer().toString();

    }

    private static String removeTagContent( String content, String field )
    {
        return content.replaceAll( "<" + field + ">.*</" + field + ">", "<" + field + "></" + field + ">" );
    }
}
