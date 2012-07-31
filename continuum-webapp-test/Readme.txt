Test with Firefox and Selenium IDE
 * Start Continuum
 * Open Firefox and navigate to Continuum (it should be on the "Create Admin User" page.)
 * in Firefox, Tools -> Selenium IDE 
 * in Selenium IDE, File -> Open Test Suite and choose src/test/selenium-ide/continuum_test_suite.html
 * in Selenium IDE, modify the Base URL if necessary (for example, http://localhost:8080/continuum)
 * in Selenium IDE, click the 'Play entire test suite' icon

Run Selenium tests in src/test/testNG with Maven, TestNG and Cargo
 * mvn clean install

Run Selenium tests against an existing Continuum instance
  * mvn clean install -DbaseUrl=http://localhost:9090/continuum

  (This skips the Cargo plugin configuration that starts a container with the Continuum webapp deployed)

Run Selenium tests in an alternate browser
  * mvn clean install -Ptomcat7x -Dbrowser=iexplore  (or -Dbrowser=safari or -Dbrowser=other -DbrowserPath=/path/to/browser)

(Note that you must specify the container in this case, as the browser profile disables the default container profile)

Change the port the embedded selenium runs on
  * mvn clean install -DseleniumPort=4444

Run Selenium tests in an running Selenium server or hub
  * mvn clean install -DseleniumHost=localhost -DseleniumPort=4444

 Note that this does not install anything, it simply runs through the lifecycle including the integration test phases.
 More properly it would be 'mvn clean post-integration-test', but install is much shorter to type. :)

If you'd like to run the tests from your IDE, you can start the container from Maven using:

    mvn generate-resources selenium:start-server cargo:run

The server will run until you press Ctrl-C, and you can run the tests from the IDE.