package eu.kiaru.ij.slidebookExportedTiffOpener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eu.kiaru.ij.controller42.devices42.CustomWFVirtualStack42;
import eu.kiaru.ij.controller42.devices42.Device42Helper;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.HyperStackConverter;

/*
 * Export Date-Time: 08/28/2017 10:26:32
 * Capture Date-Time: 8/25/2017 11:28:13
 * Z Planes: 1
 * Time Points: 5
 * Channels: 3
 * Microns Per Pixel: 0.133
 * Z Step Size Microns: 0
 * Average Timelapse Interval: 1803.67 ms (0.6 Hz) (+/- 300.0 ms)
 * IFD	X Position (um)	Y Position (um)	Z Position (um)	Elapsed Time (ms)	Channel Name	TIFF File Name
 * 0	0	0	7084.85	0	c488s	Capture 5_XY1503653285_Z0_T0_C0.tiff
 * 0	0	0	7084.85	0	c561s	Capture 5_XY1503653285_Z0_T0_C1.tiff
 * 0	0	0	7084.85	0	C640s	Capture 5_XY1503653285_Z0_T0_C2.tiff
 */
public class ImgPlusFromSlideBookLogFactory {
	static public ImagePlus getImagePlusFromLogFile(File logFile) {
		BufferedReader reader;
		if (!isLogFile(logFile)) return null;
		try {
			reader = new BufferedReader(new FileReader(logFile.getAbsolutePath()));
		    String line = reader.readLine(); // skips export date time
		    
		    //Capture Date-Time: 8/25/2017 11:28:13
		    line = reader.readLine();
		    if (!line.startsWith("Capture Date-Time:")) return null;
		    line = line.substring("Capture Date-Time:".length());
		    LocalDateTime endAcqu = fromLogFileLine(line);
		    
		    line = reader.readLine();
		    if (!line.startsWith("Z Planes:")) return null;
		    line = line.substring("Z Planes:".length()).trim();
		    int zPlanes = Integer.parseInt(line);
		    
		    line = reader.readLine();
		    if (!line.startsWith("Time Points:")) return null;
		    line = line.substring("Time Points:".length()).trim();
		    int TPs = Integer.parseInt(line);
		    
		    line = reader.readLine();
		    if (!line.startsWith("Channels:")) return null;
		    line = line.substring("Channels:".length()).trim();
		    int nChannels = Integer.parseInt(line);
		    
		    line=reader.readLine(); // skip micron per pixel
		    line=reader.readLine(); // skip zsteps
		    
		    line = reader.readLine();
		    if (!line.startsWith("Average Timelapse Interval:")) {
		    	System.err.println("Slidebook ZStack export unsupported");
		    	return null;
		    }
		    line = line.substring("Average Timelapse Interval:".length());
		    line = line.substring(0, line.indexOf("ms")).trim();
		    double avgTimeLapseInterval = Double.parseDouble(line);
		    
		    /*System.out.println(startAcqu);
		    System.out.println("zp ="+zPlanes);
		    System.out.println("TPs ="+TPs);
		    System.out.println("ch ="+nChannels);
		    System.out.println("timeinterval ="+avgTimeLapseInterval);*/
		    
		    // --------------------- ok
		    
		    //* IFD	X Position (um)	Y Position (um)	Z Position (um)	Elapsed Time (ms)	Channel Name	TIFF File Name
		    //* 0	0	0	7084.85	0	c488s	Capture 5_XY1503653285_Z0_T0_C0.tiff
		    //* 0	0	0	7084.85	0	c561s	Capture 5_XY1503653285_Z0_T0_C1.tiff
		    //* 0	0	0	7084.85	0	C640s	Capture 5_XY1503653285_Z0_T0_C2.tiff
		    line = reader.readLine();
		    Map<String, Integer> infoKeys = new HashMap<>();
		    String [] colTitles = line.split("\t");
		    for (int i=0;i<colTitles.length;i++) {
		    	infoKeys.put(colTitles[i], i);
		    	System.out.println(colTitles[i]);
		    }
		    String[][] fileNames = new String[TPs][nChannels];
		    Map<String,Integer> channelIndex = new HashMap<>();
		    int currentChannelIndex=0;
		    int frame=0;
		    int globalIndex=0;
		    if (zPlanes!=1) {
		    	System.err.println("Slidebook ZStack export unsupported");
		    	return null;

		    } else {
			    while ((line=reader.readLine())!=null) {
			    	System.out.println(line);
			    	int channel;
			    	String [] lineInfo = line.split("\t");
			    	String cChannel = lineInfo[infoKeys.get("Channel Name")];
			    	if (!channelIndex.containsKey(cChannel)) {
			    		channelIndex.put(cChannel, currentChannelIndex);
			    		currentChannelIndex++;		    		
			    	}
			    	channel = channelIndex.get(cChannel);
			    	fileNames[frame][channel] = lineInfo[infoKeys.get("TIFF File Name")];
			    	globalIndex++;
			    	if ((globalIndex % nChannels)==0) frame++;
			    }
			    
			    int[] dimensions = ExportedSBVirtualStack.getDimensions(logFile.getParent()+File.separator+fileNames[0][0]);
			    ExportedSBVirtualStack  myVirtualStack = new ExportedSBVirtualStack(logFile.getParent(),fileNames, dimensions[0], dimensions[1], TPs, nChannels, false);
		      	System.out.println(myVirtualStack==null);
			    
				ImagePlus myImpPlus = new ImagePlus(logFile.getName(), myVirtualStack);

				myImpPlus = HyperStackConverter.toHyperStack(myImpPlus, nChannels, 1, TPs);
				myVirtualStack.setHyperStack(myImpPlus);

				CalibrationTimeOrigin cal = new CalibrationTimeOrigin();
				cal.setTimeUnit("ms");
				cal.frameInterval=avgTimeLapseInterval;
				cal.fps = 1000.0/avgTimeLapseInterval;
				cal.startAcquisitionTime=endAcqu.minus(Duration.ofMillis((long)avgTimeLapseInterval*(long)(TPs-1)));
				myImpPlus.setCalibration(cal);
				return myImpPlus;		    	
		    }
			//myImpPlus.addImageListener(this);
	      	
		    /*LocalTime timeIni = Device42Helper.fromCameraLogLine(firstLine);//.LocalTime.parse(firstLine,formatter);			    
		    LocalTime timeEnd = null;
		   	double avgTimeBetweenImagesInMs = 1;
		   	if (!lastLine.equals("")) {
			   	timeEnd = Device42Helper.fromCameraLogLine(lastLine);			   
			   	avgTimeBetweenImagesInMs = Duration.between(timeIni,timeEnd).dividedBy(nSamples-1).toNanos()/1e6;
		   	}
		   	startAcquisitionTime=LocalDateTime.of(this.startAcquisitionTime.toLocalDate(),timeIni);
		   	endAcquisitionTime=LocalDateTime.of(this.startAcquisitionTime.toLocalDate(),timeEnd).plus(Duration.ofNanos((long)(avgTimeBetweenImagesInMs*1e6)));
	      	
	    	this.setSamplingInfos(startAcquisitionTime, endAcquisitionTime, nSamples);
		   	reader.close();
	      	
	      	// now Fetch data and open them
	      	String attachedRawDataPrefixFile = this.logFile.getPath().substring(0, this.logFile.getPath().length()-4);
	      	
	      	
	      	myVirtualStack = new CustomWFVirtualStack42(imgSX, imgSY, this.getNumberOfSamples(), null, null);
	      	System.out.println(myVirtualStack==null);
	      	myVirtualStack.setAttachedDataPath(attachedRawDataPrefixFile);
	      	System.out.println(myVirtualStack==null);*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return null;
	}
	
	public static boolean isLogFile(File path) {
		return path.getPath().toLowerCase().endsWith(".log"); 
	}
	
	// Because slidebook export is stupidly formatted
	static public LocalDateTime fromLogFileLine(String str) {
		//8/25/2017 11:28:13
		// HORRIBLE !!!
		//int currentIndex=0;
		int nextIndex = str.indexOf("/");		
		String month = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());
		
	   	nextIndex = str.indexOf("/");
		String day = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());

		nextIndex = str.indexOf(" ");
		String year = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());

		nextIndex = str.indexOf(":");
		String hour = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());

		nextIndex = str.indexOf(":");
		String minute = str.substring(0, nextIndex);
		str=str.substring(nextIndex+1, str.length());

		String second = str;
			
		int y = Integer.parseInt(year.trim());
		int M = Integer.parseInt(month.trim());
		int d = Integer.parseInt(day.trim());
			
		int h = Integer.parseInt(hour.trim());
		int m = Integer.parseInt(minute.trim());
		double ds = Double.parseDouble(second.trim());
		int s =(int)ds;
		ds=ds-(int)ds;
		int ns = (int)(ds*1e9);
		return LocalDateTime.of(y,M,d,h,m,s,ns);
	}
	
}
