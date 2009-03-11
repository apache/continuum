package org.apache.continuum.web.test;

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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.testng.Assert;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Based on AbstractSeleniumTestCase of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractSeleniumTest
{
    public static String baseUrl;

    public static String maxWaitTimeInMs;

    private static ThreadLocal<Selenium> selenium;

    public static Properties p;

    /**
     * Initialize selenium an others properties. This method is called from BeforeSuite method of sub-class.
     */
    public void open(int number)
        throws Exception
    {
        p = new Properties();
        p.load( this.getClass().getClassLoader().getResourceAsStream( "testng.properties" ) );

        baseUrl = p.getProperty( "BASE_URL" );
        maxWaitTimeInMs = p.getProperty( "MAX_WAIT_TIME_IN_MS" );

        String seleniumHost = p.getProperty( "SELENIUM_HOST" );
        int seleniumPort = Integer.parseInt( ( p.getProperty( "SELENIUM_PORT" ) ) );
        String seleniumBrowser = p.getProperty( "SELENIUM_BROWSER" );
        final Selenium s = new DefaultSelenium( seleniumHost, seleniumPort, seleniumBrowser, baseUrl );
        selenium = new ThreadLocal<Selenium>() {
            protected Selenium initialValue() {
                return s;
            }
        };
        geSelenium().start();
    }

    protected static Selenium geSelenium()
    {
        return selenium.get();
    }

    /**
     * Close selenium session. Called from AfterSuite method of sub-class
     */
    public void close()
        throws Exception
    {
        geSelenium().stop();
    }

    // *******************************************************
    // Auxiliar methods. This method help us and simplify test.
    // *******************************************************

    public void assertFieldValue( String fieldValue, String fieldName )
    {
        assertElementPresent( fieldName );
        Assert.assertEquals( fieldValue, geSelenium().getValue( fieldName ) );
    }

    public void assertPage( String title )
    {
        Assert.assertEquals( getTitle(), title );
    }

    public String getTitle()
    {
        return geSelenium().getTitle();
    }

    public String getHtmlContent()
    {
        return geSelenium().getHtmlSource();
    }

    public void assertTextPresent( String text )
    {
        Assert.assertTrue( geSelenium().isTextPresent( text ), "'" + text + "' isn't present." );
    }

    public void assertTextNotPresent( String text )
    {
        Assert.assertFalse( geSelenium().isTextPresent( text ), "'" + text + "' is present." );
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

    public void assertCellValueFromTable( String expected, String tableElement, int row, int column )
    {
        Assert.assertEquals( expected, getCellValueFromTable( tableElement, row, column ) );
    }

    public boolean isTextPresent( String text )
    {
        return geSelenium().isTextPresent( text );
    }

    public boolean isLinkPresent( String text )
    {
        return isElementPresent( "link=" + text );
    }

    public boolean isElementPresent( String locator )
    {
        return geSelenium().isElementPresent( locator );
    }

    public void waitPage()
    {
        geSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }

    public String getFieldValue( String fieldName )
    {
        return geSelenium().getValue( fieldName );
    }

    public String getCellValueFromTable( String tableElement, int row, int column )
    {
        return geSelenium().getTable( tableElement + "." + row + "." + column );
    }

    public void selectValue( String locator, String value )
    {
        geSelenium().select( locator, "label=" + value );
    }

    public void submit()
    {
        clickLinkWithXPath( "//input[@type='submit']" );
    }

    public void assertButtonWithValuePresent( String text )
    {
        Assert.assertTrue( isButtonWithValuePresent( text ), "'" + text + "' button isn't present" );
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
        assertElementPresent( locator );
        geSelenium().click( locator );
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

            geSelenium().type( entry.getKey(), entry.getValue() );
        }
    }

    public void setFieldValue( String fieldName, String value )
    {
        geSelenium().type( fieldName, value );
    }

    public void checkField( String locator )
    {
        geSelenium().check( locator );
    }

    public void uncheckField( String locator )
    {
        geSelenium().uncheck( locator );
    }

    public boolean isChecked( String locator )
    {
        return geSelenium().isChecked( locator );
    }
}
