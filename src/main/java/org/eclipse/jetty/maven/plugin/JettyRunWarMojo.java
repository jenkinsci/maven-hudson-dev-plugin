//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.maven.plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jetty.util.PathWatcher;
import org.eclipse.jetty.util.PathWatcher.PathWatchEvent;

/**
 * <p>
 *  This goal is used to assemble your webapp into a war and automatically deploy it to Jetty.
 *  </p>
 *  <p>
 *  Once invoked, the plugin runs continuously and can be configured to scan for changes in the project and to the
 *  war file and automatically perform a hot redeploy when necessary. 
 *  </p>
 *  <p>
 *  You may also specify the location of a jetty.xml file whose contents will be applied before any plugin configuration.
 *  This can be used, for example, to deploy a static webapp that is not part of your maven build. 
 *  </p>
 *  Runs jetty on a war file
 */
@Mojo( name = "run-war", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(phase = LifecyclePhase.PACKAGE)
public class JettyRunWarMojo extends AbstractJettyMojo
{

    /**
     * The location of the war file.
     */
    @Parameter(defaultValue="${project.build.directory}/${project.build.finalName}.war", required = true)
    private File war;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        super.execute();  
    }


    @Override
    public void finishConfigurationBeforeStart() throws Exception
    {
        server.setStopAtShutdown(true); //as we will normally be stopped with a cntrl-c, ensure server stopped 
        super.finishConfigurationBeforeStart();
    }


    
    @Override
    public void configureWebApplication () throws Exception
    {
        super.configureWebApplication();
        
        webApp.setWar(war.getCanonicalPath());
    }
 


    /**
     * @see AbstractJettyMojo#configureScanner()
     */
    @Override
    public void configureScanner() throws MojoExecutionException
    {
        scanner.watch(project.getFile().toPath());
        scanner.watch(war.toPath());

        scanner.addListener(new PathWatcher.EventListListener()
        {

            @Override
            public void onPathWatchEvents(List<PathWatchEvent> events)
            {
                try
                {
                    boolean reconfigure = false;
                    for (PathWatchEvent e:events)
                    {
                        if (e.getPath().equals(project.getFile().toPath()))
                        {
                            reconfigure = true;
                            break;
                        }
                    }
                    restartWebApp(reconfigure);
                }
                catch (Exception e)
                {
                    getLog().error("Error reconfiguring/restarting webapp after change in watched files",e);
                }
            }
        });
    }


    
    
    /** 
     * @see org.eclipse.jetty.maven.plugin.AbstractJettyMojo#restartWebApp(boolean)
     */
    @Override
    public void restartWebApp(boolean reconfigureScanner) throws Exception 
    {
        getLog().info("Restarting webapp ...");
        getLog().debug("Stopping webapp ...");
        stopScanner();
        webApp.stop();
        getLog().debug("Reconfiguring webapp ...");

        checkPomConfiguration();

        // check if we need to reconfigure the scanner,
        // which is if the pom changes
        if (reconfigureScanner)
        {
            getLog().info("Reconfiguring scanner after change to pom.xml ...");
            scanner.reset();
            configureScanner();
        }

        getLog().debug("Restarting webapp ...");
        webApp.start();
        startScanner();
        getLog().info("Restart completed.");
    }

}
