Apache Continuum XML-RPC Client

To run the sample client code:

mvn exec:exec

This assumes Continuum is running on port 8080.  If not, you will need to edit
the url in the exec plugin's configuration.

-----------------

To purge old build results, change the exec plugin configuration to run BuildResultsPurge instead of SampleClient
and run 'mvn exec:exec'.