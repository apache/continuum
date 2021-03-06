package org.apache.maven.continuum.reports.surefire;
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 12 nov. 07
 */
public class ReportTestSuite
    extends DefaultHandler
{
    private List<ReportTestCase> testCases;

    private int numberOfErrors;

    private int numberOfFailures;

    private int numberOfTests;

    private String name;

    private String fullClassName;

    private String packageName;

    private float timeElapsed;

    private final NumberFormat numberFormat = NumberFormat.getInstance();

    /**
     * @noinspection StringBufferField
     */
    private StringBuffer currentElement;

    private ReportTestCase testCase;

    private List<ReportFailure> reportFailures;


    public void parse( String xmlPath )
        throws ParserConfigurationException, SAXException, IOException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();

        SAXParser saxParser = factory.newSAXParser();

        saxParser.parse( new File( xmlPath ), this );
    }

    public void startElement( String uri, String localName, String qName, Attributes attributes )
        throws SAXException
    {
        try
        {
            if ( "testsuite".equals( qName ) )
            {
                numberOfErrors = Integer.parseInt( attributes.getValue( "errors" ) );

                numberOfFailures = Integer.parseInt( attributes.getValue( "failures" ) );

                numberOfTests = Integer.parseInt( attributes.getValue( "tests" ) );

                Number time = numberFormat.parse( attributes.getValue( "time" ) );

                timeElapsed = time.floatValue();

                //check if group attribute is existing
                if ( attributes.getValue( "group" ) != null && !"".equals( attributes.getValue( "group" ) ) )
                {
                    packageName = attributes.getValue( "group" );

                    name = attributes.getValue( "name" );

                    fullClassName = packageName + "." + name;
                }
                else
                {
                    fullClassName = attributes.getValue( "name" );

                    name = fullClassName.substring( fullClassName.lastIndexOf( "." ) + 1, fullClassName.length() );

                    int lastDotPosition = fullClassName.lastIndexOf( "." );
                    if ( lastDotPosition < 0 )
                    {
                        /* no package name */
                        packageName = "";
                    }
                    else
                    {
                        packageName = fullClassName.substring( 0, lastDotPosition );
                    }
                }

                testCases = new LinkedList<ReportTestCase>();
            }
            else if ( "testcase".equals( qName ) )
            {
                currentElement = new StringBuffer();

                testCase = new ReportTestCase();

                testCase.setFullClassName( fullClassName );

                testCase.setName( attributes.getValue( "name" ) );

                testCase.setClassName( name );

                String timeAsString = attributes.getValue( "time" );

                Number time = 0;

                if ( timeAsString != null )
                {
                    time = numberFormat.parse( timeAsString );
                }

                testCase.setTime( time.floatValue() );

                testCase.setFullName( packageName + "." + name + "." + testCase.getName() );
            }
            else if ( "failure".equals( qName ) )
            {
                testCase.setFailureType( attributes.getValue( "type" ) );
                testCase.setFailureMessage( attributes.getValue( "message" ) );
            }
            else if ( "error".equals( qName ) )
            {
                testCase.setFailureType( attributes.getValue( "type" ) );
                testCase.setFailureMessage( attributes.getValue( "message" ) );
            }
        }
        catch ( ParseException e )
        {
            throw new SAXException( e.getMessage(), e );
        }
    }

    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if ( "testcase".equals( qName ) )
        {
            testCases.add( testCase );
        }
        else if ( "failure".equals( qName ) )
        {
            testCase.setFailureDetails( currentElement.toString() );
            this.addReportFailure( new ReportFailure( testCase.getFailureType(), testCase.getFailureDetails(),
                                                      testCase.getName() ) );
        }
        else if ( "error".equals( qName ) )
        {
            testCase.setFailureDetails( currentElement.toString() );
            this.addReportFailure( new ReportFailure( testCase.getFailureType(), testCase.getFailureDetails(),
                                                      testCase.getName() ) );
        }
    }

    public void characters( char[] ch, int start, int length )
        throws SAXException
    {
        String s = new String( ch, start, length );

        if ( !"".equals( s.trim() ) )
        {
            currentElement.append( s );
        }
    }

    public List<ReportTestCase> getTestCases()
    {
        return this.testCases;
    }

    public int getNumberOfErrors()
    {
        return numberOfErrors;
    }

    public void setNumberOfErrors( int numberOfErrors )
    {
        this.numberOfErrors = numberOfErrors;
    }

    public int getNumberOfFailures()
    {
        return numberOfFailures;
    }

    public void setNumberOfFailures( int numberOfFailures )
    {
        this.numberOfFailures = numberOfFailures;
    }

    public int getNumberOfTests()
    {
        return numberOfTests;
    }

    public void setNumberOfTests( int numberOfTests )
    {
        this.numberOfTests = numberOfTests;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getFName()
    {
        return name;
    }

    public void setFName( String name )
    {
        this.name = name;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    public float getTimeElapsed()
    {
        return this.timeElapsed;
    }

    public void setTimeElapsed( float timeElapsed )
    {
        this.timeElapsed = timeElapsed;
    }

    /*
    private List<String> parseCause( String detail )
    {
        String fullName = testCase.getFullName();
        String name = fullName.substring( fullName.lastIndexOf( "." ) + 1 );
        return parseCause( detail, name );
    }
    

    private List<String> parseCause( String detail, String compareTo )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( detail, "\n" );
        List<String> parsedDetail = new ArrayList<String>( stringTokenizer.countTokens() );

        while ( stringTokenizer.hasMoreTokens() )
        {
            String lineString = stringTokenizer.nextToken().trim();
            parsedDetail.add( lineString );
            if ( lineString.indexOf( compareTo ) >= 0 )
            {
                break;
            }
        }

        return parsedDetail;
    }
    */

    public void setTestCases( List<ReportTestCase> testCases )
    {
        this.testCases = Collections.unmodifiableList( testCases );
    }

    @SuppressWarnings( "unchecked" )
    public List<ReportFailure> getReportFailures()
    {
        return reportFailures == null ? Collections.EMPTY_LIST : reportFailures;
    }

    public void setReportFailures( List<ReportFailure> reportFailures )
    {
        this.reportFailures = reportFailures;
    }

    public void addReportFailure( ReportFailure reportFailure )
    {
        if ( this.reportFailures == null )
        {
            this.reportFailures = new LinkedList<ReportFailure>();
        }
        this.reportFailures.add( reportFailure );
    }
}
