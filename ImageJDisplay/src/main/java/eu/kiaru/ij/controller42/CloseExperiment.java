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
import eu.kiaru.ij.controller42.structTime.UniformTimeIterator;
import eu.kiaru.ij.slidebookExportedTiffOpener.ImgPlusFromSlideBookLogFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

/**
 * Todo
 */
@Plugin(type = Command.class, menuPath = "Controller 42>Close Experiment")
public class CloseExperiment implements Command {
	//
    // Parameters here
    //
	@Parameter
	DSDevicesSynchronizer synchronizer;
	
	@Parameter
	ObjectService obj;

    @Override
    public void run() {    
    	synchronizer.getDevices().values().forEach(device -> {
    		device.hideDisplay();
    		device=null;
    	});
    	synchronizer.removeAllDevices();
    	obj.removeObject(synchronizer);
    	synchronizer=null;
    }
    

}
