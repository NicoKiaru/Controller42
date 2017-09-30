package eu.kiaru.ij.controller42;

import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import eu.kiaru.ij.controller42.devices42.Device42Factory;
import eu.kiaru.ij.controller42.stdDevices.LocalDateTimeDisplayer;
import eu.kiaru.ij.controller42.stdDevices.StdDeviceFactory;
import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import eu.kiaru.ij.slidebookExportedTiffOpener.ImgPlusFromSlideBookLogFactory;
import ij.ImagePlus;

@Plugin(type = Command.class, menuPath = "Controller 42>Add Device from File")
public class AddDevice implements Command{

	@Parameter
	DSDevicesSynchronizer synchronizer;
	
	@Parameter
	File f;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
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
        
        
    	Device42Factory.linkDevices(synchronizer.getDevices());
    	device.showDisplay();	
	}
	
	
}
