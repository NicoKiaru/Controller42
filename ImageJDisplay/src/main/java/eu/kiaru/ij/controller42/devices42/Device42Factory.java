package eu.kiaru.ij.controller42.devices42;

import java.io.File;
import java.util.Map;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;

public class Device42Factory {
	
	public static DefaultSynchronizedDisplayedDevice getDevice(File path) {
		DefaultSynchronizedDisplayedDevice myDevice=null;		
		if (isLogFile(path)) {
			// Log file
			// Is it a device generated by the Matlab Controller42 app ?
			
			myDevice = Device42Helper.getController42Device(path);
			if (myDevice!=null) {
				System.out.println(path.getAbsolutePath()+" ok!");
				return myDevice;
			} else {

				System.out.println(path.getAbsolutePath()+" not ok :-(");
			}
			
		}	
		return null;
	}
	
	public static boolean isLogFile(File path) {
		return path.getPath().toLowerCase().endsWith(".log"); 
	}
	
	public static void linkDevices(Map<String,DefaultSynchronizedDisplayedDevice> devices) {
		for (DefaultSynchronizedDisplayedDevice device: devices.values()) {
			System.out.println(device.getName());
			if (device.getClass()==CamTrackerDevice42.class) {
				((CamTrackerDevice42)device).setLinkedCamera((CameraDevice42)(devices.get(((CamTrackerDevice42)device).linkedCamName)));
			}
		}
	}
	

	
}
