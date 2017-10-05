package eu.kiaru.ij.controller42;

import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import eu.kiaru.ij.controller42.devices42.CamTrackerDevice42;
import eu.kiaru.ij.controller42.devices42.Device42Factory;
import eu.kiaru.ij.controller42.stdDevices.StdDeviceFactory;
import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import eu.kiaru.ij.slidebookExportedTiffOpener.ImgPlusFromSlideBookLogFactory;
import ij.ImagePlus;

@Plugin(type = Command.class, menuPath = "Controller 42>Remove device")
public class RemoveDevice implements Command{
	// TODO
	@Parameter
	DSDevicesSynchronizer synchronizer;
	
	@Parameter
	String deviceName;
	
	@Override
	public void run() {
		
		DefaultSynchronizedDisplayedDevice device = synchronizer.getDevices().get(deviceName);
		
		synchronizer.removeDevice(device);
		
		
		device.killDisplay();
        /*	if (device!=null) {        
        		Device42Factory.linkDevices(synchronizer.getDevices());
        		device.showDisplay();
        	}*/
	}
	
}
