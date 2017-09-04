package eu.kiaru.ij.slidebookExportedTiffOpener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import eu.kiaru.ij.controller42.devices42.CustomWFVirtualStack42;
import eu.kiaru.ij.controller42.devices42.Device42Helper;
import ij.ImagePlus;

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
		try {
			reader = new BufferedReader(new FileReader(logFile.getAbsolutePath()));
		    reader.readLine(); // skips export date time
		    String lSX = reader.readLine(); // image size X
		    if (lSX.startsWith("Image Size X (pixels) =")) {
		    	imgSX = Integer.parseInt(lSX.substring("Image Size X (pixels) =".length()).trim());
		    }
		    String lSY = reader.readLine(); // image size Y
		    if (lSY.startsWith("Image Size Y (pixels) =")) {
		    	imgSY = Integer.parseInt(lSY.substring("Image Size Y (pixels) =".length()).trim());
		    }
		    for (int i=0;i<4;i++) {
		    	reader.readLine();
		    }
		    
		    String firstLine=reader.readLine();
		    if (firstLine==null) {
		    	System.err.println("Could not find proper camera information... in device "+this.getName());
		    	reader.close();
		    	return;
		    }
		    String lastLine = "";
		    String sCurrentLine;
		    int nSamples=1;
		    while ((sCurrentLine = reader.readLine()) != null) 
		    {
		        lastLine = sCurrentLine;
		        nSamples++;
		    }
		    LocalTime timeIni = Device42Helper.fromCameraLogLine(firstLine);//.LocalTime.parse(firstLine,formatter);			    
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
	      	System.out.println(myVirtualStack==null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
}
