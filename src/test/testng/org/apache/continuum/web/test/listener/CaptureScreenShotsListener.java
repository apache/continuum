package org.apache.continuum.web.test.listener;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.continuum.web.test.parent.ThreadSafeSeleniumSession.getSession;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class CaptureScreenShotsListener
    extends TestListenerAdapter
{
    private static final String FS = File.separator;

    @Override
    public void onTestFailure( ITestResult tr )
    {
        captureError( tr );
        super.onTestFailure( tr );
    }

    private void captureError( ITestResult tr )
    {
        String baseFileName = getBaseFileName( tr );
        try
        {
            captureScreenshot( baseFileName );
        }
        catch ( RuntimeException e )
        {
            /* ignore errors related to captureEntirePageScreenshot not implemented in some browsers */
            if ( !e.getMessage().contains( "captureEntirePageScreenshot is only implemented for Firefox" ) )
            {
                System.out.println( "Error when taking screenshot for test " + tr.getName() + " ["
                    + getSession().getBrowser() + "]" );
                e.printStackTrace();
            }
        }
        try
        {
            captureHtmlSource( baseFileName );
        }
        catch ( IOException e )
        {
            System.out.println( "Error capturing HTML for test " + tr.getName() + " [" + getSession().getBrowser()
                + "]" );
            e.printStackTrace();
        }
    }

    private String getBaseFileName( ITestResult tr )
    {
        File f = new File( "" );
        String filePath = f.getAbsolutePath();
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd-HH_mm_ss" );
        String time = sdf.format( d );
        File targetPath = new File( filePath + FS + "target" + FS + "screenshots" );
        targetPath.mkdir();
        String cName = tr.getTestClass().getName();
        StackTraceElement stackTrace[] = tr.getThrowable().getStackTrace();
        int index = getStackTraceIndexOfCallingClass( cName, stackTrace );
        String methodName = index >= 0 ? stackTrace[index].getMethodName() : tr.getMethod().getMethodName();
        int lNumber = index >= 0 ? stackTrace[index].getLineNumber() : -1;
        String lineNumber = Integer.toString( lNumber );
        String className = cName.substring( cName.lastIndexOf( '.' ) + 1 );
        String fileName =
            targetPath.toString() + FS + methodName + "(" + className + ".java_" + lineNumber + ")-"
                + getSession().getBrowser() + "-" + time;
        return fileName;
    }

    /**
     * Save the screenshot of the browser as a PNG image
     * 
     * @param baseFileName
     */
    private void captureScreenshot( String baseFileName )
    {
        getSession().getSelenium().windowMaximize();
        getSession().getSelenium().captureEntirePageScreenshot( baseFileName + ".png", "" );
    }

    private void captureHtmlSource( String baseFileName )
        throws IOException
    {
        FileUtils.writeStringToFile( new File( baseFileName + ".html" ), getSession().getSelenium().getHtmlSource() );
    }

    private int getStackTraceIndexOfCallingClass( String nameOfClass, StackTraceElement stackTrace[] )
    {
        boolean match = false;
        int i = 0;
        do
        {
            String className = stackTrace[i].getClassName();
            match = Pattern.matches( nameOfClass, className );
            i++;
        }
        while ( ( match == false ) && ( i < stackTrace.length ) );

        if ( !match )
        {
            /* the error happened outside of the test class, maybe pre-test */
            return -1;
        }

        i--;
        return i;
    }
}