/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package eu.kiaru.ij.controller42;

import net.imagej.ImageJ;

import java.io.File;
import java.io.FilenameFilter;

import org.scijava.command.Command;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import eu.kiaru.ij.controller42.devices42.Device42Factory;
import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import ij.IJ;
import ij.WindowManager;

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
    
    @Parameter
    private IOService ioService;
    
    @Parameter(label="Select a directory", style="directory") 
    private File myDir;

    @Override
    public void run() {        
        uiService.show("Directory : "+myDir.getAbsolutePath());
        File[] files = myDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".log");
            }
        });
        
        DSDevicesSynchronizer synchronizer= new DSDevicesSynchronizer();
        
        for (File f:files) {
        	//uiService.show("File : "+f.getAbsolutePath());
        	DefaultSynchronizedDisplayedDevice device = Device42Factory.getDevice(f);
        	if (device!=null) {
        		synchronizer.addDevice(device);
        	}        	
        }   

    	Device42Factory.linkDevices(synchronizer.getDevices());
    	for (DefaultSynchronizedDisplayedDevice device:synchronizer.getDevices().values()) {
    		device.showDisplay();
    	}
        /*uiService.show("My image", );*/
        
        /*String path = "C:\\Users\\Nico\\Desktop\\typethealphabet.png";
        
        //ij.ImageJ ij;
        IJ.open(path);
        WindowManager.getActiveWindow();*/
        /*IJ.
        Dataset dataset;
		try {
			dataset = (Dataset) ioService.open(path);
	        uiService.show(dataset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

        // show the image
        
        
        
        
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

        ij.ImageJ ij1;
        ij1 = new ij.ImageJ(null, ij.ImageJ.STANDALONE);
    	
    	final ImageJ ij2 = new ImageJ();
        ij2.ui().showUI();
        ij2.command().run(LoadExperiment.class, true);        
        
    }

}
