Test with Firefox and Selenium IDE
 * Start Continuum
 * Open Firefox and navigate to Continuum (it should be on the "Create Admin User" page.)
 * in Firefox, Tools -> Selenium IDE 
 * in Selenium IDE, File -> Open Test Suite and choose src/test/selenium-ide/continuum_test_suite.html
 * in Selenium IDE, modify the Base URL if necessary (for example, http://localhost:8080/continuum)
 * in Selenium IDE, click the 'Play entire test suite' icon

Run Selenium tests in src/test/java with Maven
 * Start Continuum, create admin user, and save General Configuration
 * modify src/test/resources/it.properties as needed
 * mvn clean install

Run Selenium tests in src/test/it with Maven
 * modify src/test/resources/it.properties as needed
 * mvn clean install -f pom-it.xml

Run Selenium teests in src/test/pom2 with Maven
 * set TOMCAT_HOME environment variable to location of Tomcat 5.5.x installation
 * modify src/test/resources/it.properties as needed
 * mvn clean install -f pom2.xml

======= OLD INSTRUCTIONS FOR src/test/it BELOW =======

Test Continuum with Tomcat 5.x and firefox
    'mvn clean install' or 'mvn clean install -Ptomcat5x,firefox'

Test Continuum with Tomcat 5.x and Internet Explorer
    'mvn clean install -Ptomcat5x,iexplore'

Test Continuum with Tomcat 5.x and a specific browser
    'mvn clean install -Ptomcat5x,otherbrowser -DbrowserPath=PATH_TO_YOUR_BROWSER'

Test Continuum with Tomcat 5.x and firefox wherein your firefox executable is not in the default installation directory
    'mvn clean install' or 'mvn clean install -Ptomcat5x,firefox -Dbrowser="*firefox <full path of firefox executable>'

Test Continuum with Tomcat 5.x and Internet Explorer wherein your Internet Explorer executable is not in the default installation directory
    'mvn clean install' or 'mvn clean install -Ptomcat5x,firefox -Dbrowser="*iexplore <full path of Internet Explorer executable>'

WARNING: If you specify your own custom browser, it's up to you to configure it correctly. At a minimum, you'll need to configure your browser to use the Selenium Server as a proxy, and disable all browser-specific prompting.
http://release.openqa.org/selenium-remote-control/nightly/doc/java/com/thoughtworks/selenium/DefaultSelenium.html#DefaultSelenium(java.lang.String,%20int,%20java.lang.String,%20java.lang.String)

If you'd like to run the tests from your IDE, you can start the container from Maven using:

    mvn selenium:start-server cargo:start

The server will run until you press Ctrl-C, and you can run the tests from the IDE.
