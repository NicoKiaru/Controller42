package eu.kiaru.ij.controller42.stdDevices;

import java.io.File;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import ij.ImagePlus;

public class StdDeviceFactory {
	public static boolean isTiffFile(File path) {
		return path.getPath().toLowerCase().endsWith(".tiff"); 
	}
	
	public static DefaultSynchronizedDisplayedDevice getDevice(File path) {
		DefaultSynchronizedDisplayedDevice myDevice=null;		
		System.out.println("on y va avec"+path.getName());
		if (isTiffFile(path)) {
			myDevice = new ImagePlusDeviceUniformlySampled();
			myDevice.initDevice(path, 0);
			return myDevice;			
		}	else {
			System.out.println(path.getName()+" is not tiff!");			
		}
		return null;
	}
	
	public static ImagePlusDeviceUniformlySampled getDevice(ImagePlus img) {
		ImagePlusDeviceUniformlySampled myDevice=null;	
		System.err.println("All ImagePlus images are currently assumed to be uniformly sampled.");
		myDevice = new ImagePlusDeviceUniformlySampled();
		myDevice.initDevice(img);
		return myDevice;
	}
	
}
