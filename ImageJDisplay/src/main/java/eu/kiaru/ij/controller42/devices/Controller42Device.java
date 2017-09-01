package eu.kiaru.ij.controller42.devices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import eu.kiaru.ij.controller42.DefaultSynchronizedDisplayedDevice;

abstract public class Controller42Device extends DefaultSynchronizedDisplayedDevice {
	LocalDateTime dateOfFileCreated;
	File logFile;
	
	abstract void init42Device();
	
	public static String defautHeaderDevice42Line1 = "============";
	public static String defautHeaderDevice42Line2Prefix = "TYPE =";
	public static String defautHeaderDevice42Line3Prefix = "Log file for device";
	public static String defautHeaderDevice42Line4Prefix = "File created on (yy-mm-dd) ";
	private static final Map<String, Class> devices42Map;
    static {
        Map<String, Class> iMap = new HashMap<>();
        iMap.put("Aladdin", null);
        iMap.put("CamTracker", CamTrackerDevice.class);
        iMap.put("MP_285", null);
        iMap.put("NikonTIControl", null);
        iMap.put("Shutter", null);
        iMap.put("Shutter_Listen", null);
        iMap.put("Thorlabs_SH05", null);
        iMap.put("Tracker", null);
        iMap.put("Tracker_Focus", null);
        iMap.put("UserNotification", null);
        iMap.put("Zaber", ZaberDevice.class);
        iMap.put("ZDrive_Nikon", null);
        iMap.put("Camera", CameraDevice.class);
        devices42Map = Collections.unmodifiableMap(iMap);
    }
	
	/*
	 * Aladdin_Device
	 * CamTracker_Device
	 * MP_285_Device
	 * NikonTIControl_Device
	 * Shutter_Device
	 * Shutter_Listen_Device
	 * TestAG
	 * Thorlabs_SH05_Device
	 * Tracker_Device
	 * Tracker_Focus_Device
	 * UserNotification_Device
	 * Zaber_Device
	 * ZDriveNikon_Device
	 */
	
	
	public static Controller42Device getController42Device(File path) {
		try
		{
		    BufferedReader reader = new BufferedReader(new FileReader(path.getAbsolutePath()));
		    String line;
		    Controller42Device ans = null;
		    if ((line = reader.readLine()) != null)
		      if (line.equals(defautHeaderDevice42Line1))
		    	  if ((line = reader.readLine()) != null)				    
				      if (line.startsWith(defautHeaderDevice42Line2Prefix)) {
				    	  String deviceTypeName = line.trim().substring(defautHeaderDevice42Line2Prefix.length()).trim();
				    	  System.out.print("Type:"+deviceTypeName+"[");
				    	  Class deviceClass=devices42Map.get(deviceTypeName);
				    	  if (deviceClass==null) {
				    		  System.out.println("Currently Unsupported]");
				    		  reader.close();
				    		  return null;
				    	  }
				    	  if ((line = reader.readLine()) != null)
				    	  if (line.startsWith(defautHeaderDevice42Line3Prefix)) {
					    	  String deviceName = line.trim().substring(defautHeaderDevice42Line3Prefix.length()).trim();
					    	  if (deviceName.trim().equals("")) {
					    		  deviceName="NoName";
					    	  }
					    	  System.out.print(deviceName);
					    	  ans = (Controller42Device) deviceClass.newInstance();
					    	  ans.setName(deviceName);
				    	  }
				    	  System.out.println("]");
				    	  ans.logFile=path;
				    	  ans.dateOfFileCreated = null;
				    	  if ((line = reader.readLine()) != null)
					      if (line.startsWith(defautHeaderDevice42Line4Prefix)) {
						   	  String strDate = line.trim().substring(defautHeaderDevice42Line4Prefix.length()).trim();
						   	  System.out.println("\t Log File created on "+strDate);
						   	  //DateFormat format = new SimpleDateFormat("yy-MM-dd 'at' HH'h'mm'm'ss.SSS's'", Locale.FRANCE);
						   	  //Date date = format.parse(strDate);

						   	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd 'at' HH'h'mm'm'ss.SSS's'");
						   	  ans.dateOfFileCreated=LocalDateTime.parse(strDate,formatter);
						   	  		   	  
					      }
				    	  ans.init42Device();
				      }	
		    reader.close();
		    return ans;
		}
		catch (Exception e)
		{
		    return null;
		}
	}
	
	@Override
	public void initDisplay() {
		// TODO Auto-generated method stub
		//isDisplayed = true;
		//showDisplay();
	}	
	
	@Override
	public void closeDisplay() {
		// TODO Auto-generated method stub
		//isDisplayed = false;
		//removeDisplay();
	}
	
}
