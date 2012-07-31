package org.apache.continuum.web.test.parent;

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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Based on AbstractSeleniumTestCase of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractSeleniumTest
{
    public static String baseUrl;

    public static String browser;

    public static String maxWaitTimeInMs;

    private static ThreadLocal<Selenium> selenium = new ThreadLocal<Selenium>();

    private static Properties p;

    private final static String PROPERTIES_SEPARATOR = "=";
    
    private static String maxProjectWaitTimeInMs;

    /**
     * Initialize selenium
     */
    public void open( String baseUrl, String browser, String seleniumHost, int seleniumPort )
        throws Exception
    {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream( "testng.properties" );
        p = new Properties();
        p.load( input );

        maxWaitTimeInMs = getProperty( "MAX_WAIT_TIME_IN_MS" );
        maxProjectWaitTimeInMs = getProperty( "MAX_PROJECT_WAIT_TIME_IN_MS" );

        AbstractSeleniumTest.baseUrl = baseUrl;

        AbstractSeleniumTest.browser = browser;

        if ( getSelenium() == null )
        {
            DefaultSelenium s = new DefaultSelenium( seleniumHost, seleniumPort, browser, baseUrl );
            s.start();
            s.setTimeout( maxWaitTimeInMs );
            selenium.set( s );
        }
    }

    public static Selenium getSelenium()
    {
        return selenium == null ? null : selenium.get();
    }

    protected String getProperty( String key )
    {
        return p.getProperty( key );
    }

    // TODO: look into removing this, as the issue should be fixed by upgrading the resources plugin to v2.4+
    protected String getEscapeProperty( String key )
    {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream( "testng.properties" );
        String value = null;
        List<String> lines;
        try
        {
            lines = IOUtils.readLines( input );
        }
        catch ( IOException e )
        {
            lines = new ArrayList<String>();
        }
        for ( String l : lines )
        {
            if ( l != null && l.startsWith( key ) )
            {
                int indexSeparator = l.indexOf( PROPERTIES_SEPARATOR );
                value = l.substring( indexSeparator + 1 ).trim();
                break;
            }
        }
        return value;
    }

    /**
     * Close selenium session. Called from AfterSuite method of sub-class
     */
    @AfterSuite(alwaysRun = true)
    public void close()
        throws Exception
    {
        if ( getSelenium() != null )
        {
            getSelenium().stop();
            selenium.set( null );
        }
    }

    // *******************************************************
    // Auxiliar methods. This method help us and simplify test.
    // *******************************************************

    public void assertFieldValue( String fieldValue, String fieldName )
    {
        assertElementPresent( fieldName );
        Assert.assertEquals( fieldValue, getSelenium().getValue( fieldName ) );
    }

    public void assertPage( String title )
    {
        Assert.assertEquals( getTitle(), title );
    }

    public String getTitle()
    {
        return getSelenium().getTitle();
    }

    public String getHtmlContent()
    {
        return getSelenium().getHtmlSource();
    }

    public void assertTextPresent( String text )
    {
        Assert.assertTrue( getSelenium().isTextPresent( text ), "'" + text + "' isn't present." );
    }

    public void assertTextNotPresent( String text )
    {
        Assert.assertFalse( getSelenium().isTextPresent( text ), "'" + text + "' is present." );
    }

    public void assertElementPresent( String elementLocator )
    {
        Assert.assertTrue( isElementPresent( elementLocator ), "'" + elementLocator + "' isn't present." );
    }

    public void assertElementNotPresent( String elementLocator )
    {
        Assert.assertFalse( isElementPresent( elementLocator ), "'" + elementLocator + "' is present." );
    }

    public void assertLinkPresent( String text )
    {
        Assert.assertTrue( isElementPresent( "link=" + text ), "The link '" + text + "' isn't present." );
    }

    public void assertLinkNotPresent( String text )
    {
            Assert.assertFalse( isElementPresent( "link=" + text ), "The link '" + text + "' is present." );
    }

    public void assertImgWithAlt( String alt )
    {
        assertElementPresent( "//img[@alt='" + alt + "']" );
    }

    public void assertImgWithAltAtRowCol( boolean isALink, String alt, int row, int column )
    {
        String locator = "//tr[" + row + "]/td[" + column + "]/";
        locator += isALink ? "a/" : "";
        locator += "img[@alt='" + alt + "']";

        assertElementPresent( locator );
    }

    public void assertImgWithAltNotPresent( String alt )
    {
        assertElementNotPresent( "//img[@alt='" + alt + "']" );
    }

    public void assertCellValueFromTable( String expected, String tableElement, int row, int column )
    {
        Assert.assertEquals( expected, getCellValueFromTable( tableElement, row, column ) );
    }

    public boolean isTextPresent( String text )
    {
        return getSelenium().isTextPresent( text );
    }

    public boolean isLinkPresent( String text )
    {
        return isElementPresent( "link=" + text );
    }

    public boolean isElementPresent( String locator )
    {
        return getSelenium().isElementPresent( locator );
    }

    public void waitPage()
    {
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }

    public String getFieldValue( String fieldName )
    {
        return getSelenium().getValue( fieldName );
    }

    public String getCellValueFromTable( String tableElement, int row, int column )
    {
        return getSelenium().getTable( tableElement + "." + row + "." + column );
    }

    public void selectValue( String locator, String value )
    {
        getSelenium().select( locator, "label=" + value );
    }

    public void assertOptionPresent( String selectField, String[] options )
    {
        assertElementPresent( selectField );
        String[] optionsPresent = getSelenium().getSelectOptions( selectField );
        List<String> expected = Arrays.asList( options );
        List<String> present = Arrays.asList( optionsPresent );

        Assert.assertTrue( present.containsAll( expected ), "Options expected are not included in present options" );
    }

    public void assertSelectedValue( String value, String fieldName )
    {
        assertElementPresent( fieldName );
        String optionsPresent = getSelenium().getSelectedLabel( value );

        Assert.assertEquals( optionsPresent, value );
    }

    public void submit()
    {
        clickLinkWithXPath( "//input[@type='submit']" );
    }

    public void assertButtonWithValuePresent( String text )
    {
        Assert.assertTrue( isButtonWithValuePresent( text ), "'" + text + "' button isn't present" );
    }

    public void assertButtonWithIdPresent( String id )
    {
        Assert.assertTrue( isButtonWithIdPresent( id ), "'Button with id =" + id + "' isn't present" );
    }

    public void assertButtonWithValueNotPresent( String text )
    {
        Assert.assertFalse( isButtonWithValuePresent( text ), "'" + text + "' button is present" );
    }

    public boolean isButtonWithValuePresent( String text )
    {
        return isElementPresent( "//button[@value='" + text + "']" )
            || isElementPresent( "//input[@value='" + text + "']" );
    }

    public boolean isButtonWithIdPresent( String text )
    {
        return isElementPresent( "//button[@id='" + text + "']" ) || isElementPresent( "//input[@id='" + text + "']" );
    }

    public void clickButtonWithValue( String text )
    {
        clickButtonWithValue( text, true );
    }

    public void clickButtonWithValue( String text, boolean wait )
    {
        assertButtonWithValuePresent( text );

        if ( isElementPresent( "//button[@value='" + text + "']" ) )
        {
            clickLinkWithXPath( "//button[@value='" + text + "']", wait );
        }
        else
        {
            clickLinkWithXPath( "//input[@value='" + text + "']", wait );
        }
    }

    public void clickSubmitWithLocator( String locator )
    {
        clickLinkWithLocator( locator );
    }

    public void clickSubmitWithLocator( String locator, boolean wait )
    {
        clickLinkWithLocator( locator, wait );
    }

    public void clickImgWithAlt( String alt )
    {
        clickLinkWithLocator( "//img[@alt='" + alt + "']" );
    }

    public void clickLinkWithText( String text )
    {
        clickLinkWithText( text, true );
    }

    public void clickLinkWithText( String text, boolean wait )
    {
        clickLinkWithLocator( "link=" + text, wait );
    }

    public void clickLinkWithXPath( String xpath )
    {
        clickLinkWithXPath( xpath, true );
    }

    public void clickLinkWithXPath( String xpath, boolean wait )
    {
        clickLinkWithLocator( "xpath=" + xpath, wait );
    }

    public void clickLinkWithLocator( String locator )
    {
        clickLinkWithLocator( locator, true );
    }

    public void clickLinkWithLocator( String locator, boolean wait )
    {
        getSelenium().click( locator );
        if ( wait )
        {
            waitPage();
        }
    }

    public void setFieldValues( Map<String, String> fieldMap )
    {
        Map.Entry<String, String> entry;

        for ( Iterator<Entry<String, String>> entries = fieldMap.entrySet().iterator(); entries.hasNext(); )
        {
            entry = entries.next();

            getSelenium().type( entry.getKey(), entry.getValue() );
        }
    }

    public void setFieldValue( String fieldName, String value )
    {
        getSelenium().type( fieldName, value );
    }

    public void checkField( String locator )
    {
        getSelenium().check( locator );
    }

    public void uncheckField( String locator )
    {
        getSelenium().uncheck( locator );
    }

    public boolean isChecked( String locator )
    {
        return getSelenium().isChecked( locator );
    }

    public void assertIsChecked( String locator )
    {
        Assert.assertTrue( getSelenium().isChecked( locator ) );
    }

    public void assertIsNotChecked( String locator )
    {
        Assert.assertFalse( getSelenium().isChecked( locator ) );
    }

    public void click( String locator )
    {
        getSelenium().click( locator );
    }

    public void clickAndWait( String locator )
    {
        getSelenium().click( locator );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }

    public void waitForElementPresent( String locator )
        throws Exception
    {
        waitForElementPresent( locator, true );
    }
    
    /*
     * This will wait for the condition to be met.
     *   * shouldBePresent - if the locator is expected or not (true or false respectively)
     */
    public void waitForElementPresent( String locator, boolean shouldBePresent )
        throws Exception
    {
        if ( browser.equals( "*iexplore" ) )
        {
            int currentIt = 0;
            int maxIt = Integer.valueOf( getProperty( "WAIT_TRIES" ) );
            String pageLoadTimeInMs = getProperty( "PAGE_LOAD_TIME_IN_MS" );
            
            while ( isElementPresent( locator ) != shouldBePresent && currentIt < maxIt )
            {
                getSelenium().waitForPageToLoad( pageLoadTimeInMs );
                currentIt++;
            }
        }
        else
        {
            String condition = "selenium.isElementPresent(\"" + locator + "\") == " + shouldBePresent;
            waitForCondition( condition );
        }
    }

    public void selectForOption( String locator, String text )
    {
        getSelenium().addSelection( locator, "label=" + text );
    }

    public void unselectForOption( String locator, String text )
    {
        getSelenium().removeSelection( locator, "label=" + text );
    }
    
    public void waitForCondition( String condition )
        throws Exception
    {
        getSelenium().waitForCondition( condition, maxProjectWaitTimeInMs );
    }

    public void assertEnabled( String locator )
    {
        Assert.assertTrue( getSelenium().isEditable( locator ), "'" + locator + "' is disabled" );
    }

    public void assertDisabled( String locator )
    {
        Assert.assertFalse( getSelenium().isEditable( locator ), "'" + locator + "' is enabled" );
    }
}
