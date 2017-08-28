/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package eu.kiaru.ij;

import net.imagej.ImageJ;

import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 * This plugin show a minimal example for using JOGL dependency in an IJ Command.
 * Natives loading is failing with JOGL with an ImageJ2 command, as of 15th August 2017
 * see http://forum.imagej.net/t/fiji-command-unable-to-find-jogl-library-minimal-example/6484/28
 *  - > this branch shows how to overcome the problem by executing a dummy groovy script
 *  groovy execution with the proper import is able to load correclty the natives
 *  // TO FIX
 *  It works, but it's ugly, but it works, but it's ugly. 
 *  
 *  Note the JOGLLoader class should be changed for other versions of JOGL (GL3 / GL4...)
 */
@Plugin(type = Command.class, menuPath = "Controller 42>Load Experiment")
public class LoadExperiment implements Command {
	//
    // Parameters here
    //
    @Parameter
    private UIService uiService;
    
    @Parameter(label="Select a directory", style="directory") 
    private File myDir;
    

    @Override
    public void run() {
        // uiService.show("Running MinJOGLIJCommand");
        // Creates a new JOGL window
        // uiService.show("A new JOGL window has appeared. It should change color randomly upon image resizing. Try it!");
        uiService.show(myDir.getAbsolutePath());
        
        
    }
    
    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
        ij.command().run(LoadExperiment.class, true);
    }

}
