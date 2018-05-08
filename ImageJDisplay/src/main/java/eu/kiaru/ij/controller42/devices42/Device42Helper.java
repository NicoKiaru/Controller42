package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;

public class Device42Helper {
	
	
	public static String defautHeaderDevice42Line1       = "============";
	public static String defautHeaderDevice42Line2Prefix = "Log file for device";
	public static String defautHeaderDevice42Line3Prefix = "File created on (yy-mm-dd)";
	public static String defautHeaderDevice42Line4Prefix = "Type ";
	public static String defautHeaderDevice42Line5Prefix = "Version ";
	
	private static final Map<String, Class> devices42Map;
    static {
        Map<String, Class> iMap = new HashMap<>();
        iMap.put("ALADDIN", null);
        iMap.put("TRACKER", CamTrackerDevice42.class);
        iMap.put("MP_285", MP285Device42.class);
        iMap.put("NikonTIControl", null);
        iMap.put("Shutter", null);
        iMap.put("Shutter_Listen", null);
        iMap.put("Thorlabs_SH05", null);
        iMap.put("Tracker", null);
        iMap.put("Tracker_Focus", null);
        iMap.put("UserNotification", null);
        iMap.put("ZABER", ZaberDevice42.class);
        iMap.put("ZDrive_Nikon", null);
        iMap.put("CAMERA", CameraDevice42.class);
        devices42Map = Collections.unmodifiableMap(iMap);
    }
    
    private static final Map<String, Class> devices42MapV1Compatibility;
    static {
        Map<String, Class> iMap = new HashMap<>();
        iMap.put("Aladdin", null);
        iMap.put("CAMERA_GUPPY.LOG", CameraDevice42.class);
        iMap.put("MP_285", MP285Device42.class);
        iMap.put("NikonTIControl", null);
        iMap.put("Shutter", null);
        iMap.put("Shutter_Listen", null);
        iMap.put("Thorlabs_SH05", null);
        iMap.put("BEAD_TRACKER.LOG", CamTrackerDevice42.class);
        iMap.put("Tracker_Focus", null);
        iMap.put("UserNotification", null);
        iMap.put("ZABER_RIGHT.LOG", ZaberDevice42V1.class);
        iMap.put("ZDrive_Nikon", null);
        iMap.put("Camera", CameraDevice42.class);
        devices42MapV1Compatibility = Collections.unmodifiableMap(iMap);
    }
    
    final public static int IS_NOT_DEVICE42=0;
    final public static int IS_DEVICE42_V1=1;
    final public static int IS_DEVICE42_V2=2;
    
    public static int getController42LogFileVersion(File path) {
    	try	{
    		BufferedReader reader = new BufferedReader(new FileReader(path.getAbsolutePath()));
    		try {	    		
    			String line;
			    if ((line = reader.readLine()) == null)  			    {return IS_NOT_DEVICE42;}
			    if (!line.startsWith(defautHeaderDevice42Line1)) 			{return IS_NOT_DEVICE42;}
			    if ((line = reader.readLine()) == null) 	 			{return IS_NOT_DEVICE42;}
			 	if (!line.startsWith(defautHeaderDevice42Line2Prefix))  {return IS_NOT_DEVICE42;}
			 	if ((line = reader.readLine()) == null) 	 			{return IS_NOT_DEVICE42;}
			 	if (!line.startsWith(defautHeaderDevice42Line3Prefix))  {return IS_NOT_DEVICE42;}
			 	if ((line = reader.readLine()) == null) 	 			{return IS_NOT_DEVICE42;}
			 	if (line.startsWith(defautHeaderDevice42Line4Prefix))  {
			 		return IS_DEVICE42_V2;
			 	} else {
			 		return IS_DEVICE42_V1;
			 	}
			} finally {reader.close();}
		} catch (Exception e) {
			e.printStackTrace();
			return IS_NOT_DEVICE42;
		}
    }
    
    static public DefaultSynchronizedDisplayedDevice initDevice42_V1(File path) {
    	System.out.println("Initializing device V1...");
    	DefaultSynchronizedDisplayedDevice device = null;
    	try
		{   
    		BufferedReader reader = new BufferedReader(new FileReader(path.getAbsolutePath()));
	    	try {
			    String line;		    
			    line = reader.readLine();
			    line = reader.readLine();				    
					String deviceName = line.trim().substring(defautHeaderDevice42Line2Prefix.length()).trim();
					System.out.println("Name:"+deviceName);
				line = reader.readLine(); // line 3 -> starting Date
					String strDate = line.trim().substring(defautHeaderDevice42Line3Prefix.length()).trim();
					System.out.println("\t Log File created on "+strDate);
					LocalDateTime startTime = fromLogFileLine(strDate);//LocalDateTime.parse(strDate,formatter);		   	  		   	  
		    	/*line = reader.readLine(); // line 4 -> device type
		    		String deviceTypeName = line.trim().substring(defautHeaderDevice42Line4Prefix.length()).trim();
		    		Class deviceClass= devices42Map.get(deviceTypeName);
		    		if (deviceClass==null) {
		    			System.out.println("Currently Unsupported]");
		    			return null;
		    		}*/
					
				Class deviceClass= devices42MapV1Compatibility.get(path.getName().toUpperCase());
				if (deviceClass==null) {
	    			System.out.println("Currently Unsupported]");
	    			return null;
	    		}
				
				
				device = (DefaultSynchronizedDisplayedDevice) deviceClass.newInstance();
				device.setName(deviceName);
				device.initDevice(path,1);
				device.startAcquisitionTime=startTime;
			    device.initDevice();
			    //
			    if (deviceClass.equals(CamTrackerDevice42.class)) {
			    	 ((CamTrackerDevice42) device).linkedCamName="CAMERA_GUPPY";
			    }
			    return device;
		    } finally {
		    	reader.close();
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		    return null;
		}
    }
    
    static public DefaultSynchronizedDisplayedDevice initDevice42_V2(File path) {   
    	System.out.println("Initializing device V2...");
    	DefaultSynchronizedDisplayedDevice device = null;
    	try
		{   
    		BufferedReader reader = new BufferedReader(new FileReader(path.getAbsolutePath()));
	    	try {
			    String line;		    
			    line = reader.readLine();
			    line = reader.readLine();				    
					String deviceName = line.trim().substring(defautHeaderDevice42Line2Prefix.length()).trim();
					System.out.println("Name:"+deviceName);
				line = reader.readLine(); // line 3 -> starting Date
					String strDate = line.trim().substring(defautHeaderDevice42Line3Prefix.length()).trim();
					System.out.println("\t Log File created on "+strDate);
					LocalDateTime startTime = fromLogFileLine(strDate);//LocalDateTime.parse(strDate,formatter);		   	  		   	  
		    	line = reader.readLine(); // line 4 -> device type
		    		String deviceTypeName = line.trim().substring(defautHeaderDevice42Line4Prefix.length()).trim();		    		
		    		Class deviceClass= devices42Map.get(deviceTypeName);
		    		if (deviceClass==null) {
		    			System.out.println("Currently Unsupported");
		    			return null;
		    		}
		    	line = reader.readLine(); // line 4 -> device type		    	
		    		String strVersion = line.trim().substring(defautHeaderDevice42Line5Prefix.length()).trim();
		    		
				device = (DefaultSynchronizedDisplayedDevice) deviceClass.newInstance();
				device.setName(deviceName);				
				device.initDevice(path,(int)(Float.parseFloat(strVersion)));
				device.startAcquisitionTime=startTime;
			    device.initDevice();			    
			    return device;
		    } finally {
		    	reader.close();
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		    return null;
		}
    }
    
    public static DefaultSynchronizedDisplayedDevice getController42Device(File path) {
    	DefaultSynchronizedDisplayedDevice device = null;
    	switch (getController42LogFileVersion(path)) {
			case IS_NOT_DEVICE42:
				break;
			case IS_DEVICE42_V1:
				device = initDevice42_V1(path);
				break;
			case IS_DEVICE42_V2:
				device = initDevice42_V2(path);
				break;
		}
    	return device;    	
	}
	
	// Because I was stupid in Matlab
	static public LocalDateTime fromLogFileLine(String str) {
		//17-08-25 at 11h13m21.101s
		//16-11-16 at 15h57m3.442s
		//String[] parts = str.split("(\\d+)-(\\d+)-(\\d+)");
	   	//System.out.println(str);
		// HORRIBLE !!!
		//int currentIndex=0;
		int nextIndex = str.indexOf("-");		
		String year = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		nextIndex = str.indexOf("-");
		String month = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		nextIndex = str.indexOf(" ");
		String day = str.substring(0, nextIndex);
		str=str.substring(nextIndex+4, str.length());
		
		nextIndex = str.indexOf("h");
		String hour = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		nextIndex = str.indexOf("m");
		String minute = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		nextIndex = str.indexOf("s");
		String second = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		int y = Integer.parseInt(year);
		int M = Integer.parseInt(month);
		int d = Integer.parseInt(day);
		
		int h = Integer.parseInt(hour);
		int m = Integer.parseInt(minute);
		double ds = Double.parseDouble(second);
		int s =(int)ds;
		ds=ds-(int)ds;
		int ns = (int)(ds*1e9);
		return LocalDateTime.of(y,M,d,h,m,s,ns);
	}
	
	
	
	static public LocalTime fromCameraLogLine(String str) {

		str = str.substring(str.indexOf('\t')+1);
		int nextIndex = str.indexOf("\t");
		String hour = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		nextIndex = str.indexOf("\t");
		String minute = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		String second = str;
		
		int h = Integer.parseInt(hour);
		int m = Integer.parseInt(minute);
		double ds = Double.parseDouble(second);
		int s =(int)ds;
		ds=ds-(int)ds;
		int ns = (int)(ds*1e9);
		return LocalTime.of(h, m, s, ns);
	}	
	
	static public LocalTime fromMP285LogLine(String str) {

		int nextIndex = str.indexOf("\t");
		String hour = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		nextIndex = str.indexOf("\t");
		String minute = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
		nextIndex = str.indexOf("\t");
		String second = str.substring(0, nextIndex);
		
		int h = Integer.parseInt(hour);
		int m = Integer.parseInt(minute);
		double ds = Double.parseDouble(second);
		int s =(int)ds;
		ds=ds-(int)ds;
		int ns = (int)(ds*1e9);
		return LocalTime.of(h, m, s, ns);
	}
	
}
