package eu.kiaru.ij.controller42.devices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import eu.kiaru.ij.controller42.DefaultSynchronizedDisplayedDevice;

public class DeviceFactory {
	
	public static DefaultSynchronizedDisplayedDevice getDevice(File path) {
		if (isLogFile(path)) {
			// Log file
			// Is it a device generated by the Matlab Controller42 app ?
			if (isStdController42LogFile(path)) {
				System.out.println(path.getAbsolutePath()+ " is a 42 device.");
			}
			
		}	
		return null;
	}
	
	public static boolean isLogFile(File path) {
		return path.getPath().toLowerCase().endsWith(".log"); 
	}
	
	public static String defautHeaderDevice42Line1 = "============";
	public static String defautHeaderDevice42Line2Prefix = "Log file for device";
	
	public static boolean isStdController42LogFile(File path) {
		try
		{
		    BufferedReader reader = new BufferedReader(new FileReader(path.getAbsolutePath()));
		    String line;
		    boolean ans = false;
		    if ((line = reader.readLine()) != null)
		      if (line.equals(defautHeaderDevice42Line1))
		    	  if ((line = reader.readLine()) != null)				    
				      if (line.startsWith(defautHeaderDevice42Line2Prefix)) 
				    	  ans=true;
		    return ans;
		}
		catch (Exception e)
		{
		    return false;
		}
	}
	
}
