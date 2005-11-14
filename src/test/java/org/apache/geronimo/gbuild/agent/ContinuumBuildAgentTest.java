package org.apache.geronimo.gbuild.agent;
/**
 * @version $Rev$ $Date$
 */

import junit.framework.*;
import org.apache.geronimo.gbuild.agent.ContinuumBuildAgent;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.embed.Embedder;

import java.io.File;
import java.io.IOException;

public class ContinuumBuildAgentTest extends TestCase {
    ContinuumBuildAgent continuumBuildAgent;

    private CVS cvs;
    private File shellScript;

    protected void setUp() throws Exception {

        File root = new File("/Users/dblevins/work/gbuild/trunk/target/it").getCanonicalFile();
        File cvsroot = new File(root, "cvs-root");
        File module = new File(root, "shell");

        deleteAndCreateDirectory( module );

        shellScript = createScript(module);

        cvs = new CVS(cvsroot);
        cvs.init();
        cvs._import(module, "shell");

    }

    public void testBuild() throws Exception {

        Project project = new Project();
        project.setId(10);
        project.setScmUrl("scm|cvs|local|" + cvs.getCvsroot().getAbsolutePath() + "|shell");
        project.setName("Shell Project");
        project.setVersion("3.0");
        project.setExecutorId(ShellBuildExecutor.ID);
        project.setState(ContinuumProjectState.OK);

        BuildDefinition bd = new BuildDefinition();
        bd.setId(20);
        bd.setBuildFile(shellScript.getAbsolutePath());
        bd.setArguments("");

        project.addBuildDefinition(bd);

        MapContinuumStore store = new MapContinuumStore();

        store.updateProject(project);
        store.storeBuildDefinition(bd);

        ThreadContextContinuumStore.setStore(store);

        Embedder embedder = new Embedder();
        embedder.start();
        ContinuumBuildAgent buildAgent = (ContinuumBuildAgent) embedder.lookup(BuildAgent.ROLE);

        buildAgent.init();
        buildAgent.build(project.getId(), bd.getId(), 1);

        int latestBuildId = project.getLatestBuildId();
        BuildResult buildResult = store.getBuildResult(latestBuildId);

        assertNotNull("buildResult",buildResult);
        assertEquals("buildResult.getState",ContinuumProjectState.OK, buildResult.getState());
        assertEquals("project.getState",ContinuumProjectState.OK, project.getState());
    }

    public static void deleteAndCreateDirectory(File directory)
            throws IOException {
        if (directory.isDirectory()) {
            FileUtils.deleteDirectory(directory);
        }

        assertTrue("Could not make directory " + directory, directory.mkdirs());
    }

    private File createScript(File module) throws IOException, CommandLineException {
        File script;

        String EOL = System.getProperty( "line.separator" );

        boolean isWindows = System.getProperty("os.name").startsWith("Windows");

        boolean isCygwin = "true".equals(System.getProperty("cygwin"));

        if ( isWindows && !isCygwin ) {

            script = new File( module, "script.bat" );

            String content = "@ECHO OFF" + EOL
                    + "IF \"%*\" == \"\" GOTO end" + EOL
                    + "FOR %%a IN (%*) DO ECHO %%a" + EOL
                    + ":end" + EOL;

            FileUtils.fileWrite( script.getAbsolutePath(), content );

        } else {

            script = new File( module, "script.sh" );

            String content = "#!/bin/bash" + EOL + "for arg in \"$@\"; do echo $arg ; done"+EOL;

            FileUtils.fileWrite( script.getAbsolutePath(), content );

            Chmod.exec(module, "+x", script);
        }

        return script;
    }

}