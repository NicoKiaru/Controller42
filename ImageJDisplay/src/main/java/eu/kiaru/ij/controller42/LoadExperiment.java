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
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.scijava.command.Command;
import org.scijava.io.IOService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import eu.kiaru.ij.controller42.devices42.CamTrackerDevice42;
import eu.kiaru.ij.controller42.devices42.Device42Factory;
import eu.kiaru.ij.controller42.devices42.ZaberDevice42;
import eu.kiaru.ij.controller42.stdDevices.ImagePlusDeviceUniformlySampled;
import eu.kiaru.ij.controller42.stdDevices.LocalDateTimeDisplayer;
import eu.kiaru.ij.controller42.stdDevices.StdDeviceFactory;
import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import eu.kiaru.ij.controller42.structTime.TimeIterator;
import eu.kiaru.ij.slidebookExportedTiffOpener.ImgPlusFromSlideBookLogFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

/**
 * Todo
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
    
    @Parameter
    private ObjectService objService;
    
    @Parameter(label="Select a directory", style="directory") 
    private File myDir;

    @Override
    public void run() {        
    	String syncId = "Exp@"+myDir.getAbsolutePath();
    	
        uiService.show("Directory : "+myDir.getAbsolutePath());
        File[] files = myDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(".log"));
                	  //||name.toLowerCase().endsWith(".tif")
                	  //||name.toLowerCase().endsWith(".tiff"));
            }
        });
        
        DSDevicesSynchronizer synchronizer= new DSDevicesSynchronizer();
        
        for (File f:files) {
        	boolean initialized=false;
        	//uiService.show("File : "+f.getAbsolutePath());
        	DefaultSynchronizedDisplayedDevice device;
        	device = Device42Factory.getDevice(f);
        	if (device!=null) {
        		synchronizer.addDevice(device);
        		initialized=true;
        	}

        	if (!initialized) {
        		ImagePlus imSlideBook = ImgPlusFromSlideBookLogFactory.getImagePlusFromLogFile(f);
        		if (imSlideBook!=null) {
        			device = StdDeviceFactory.getDevice(imSlideBook);
    	        	if (device!=null) {
    	        		synchronizer.addDevice(device);
    	        		initialized=true;
    	        	}
        		}
	        }
        }   
        
        LocalDateTimeDisplayer timeDisplay = new LocalDateTimeDisplayer();
        timeDisplay.setName("Displayed time of experiment "+myDir.getAbsolutePath());
        synchronizer.addDevice(timeDisplay);
        timeDisplay.idSynchronizer = syncId;
        
    	Device42Factory.linkDevices(synchronizer.getDevices());
    	for (DefaultSynchronizedDisplayedDevice device:synchronizer.getDevices().values()) {
    		device.showDisplay();
    	}
    	
    	
        synchronizer.id=syncId;
    	objService.addObject(synchronizer);
    	
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
