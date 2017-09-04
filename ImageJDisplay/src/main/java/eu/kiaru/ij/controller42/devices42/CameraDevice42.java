package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import ij.ImageListener;
import ij.ImagePlus;
import ij.process.ImageProcessor;


public class CameraDevice42 extends UniformlySampledSynchronizedDisplayedDevice<ImageProcessor> implements ImageListener {
	/* Typical Header (V2):
	 * ============
	 * TYPE = Camera
	 * Log file for device CAMERA_GUPPY
	 * File created on (yy-mm-dd) 17-08-25 at 11h13m21.101s
	 * Camera Informations
	 * 	TIME_START=2017,8,25,11,13,21.106000
	 * Image Size X (pixels) = 780
	 * Image Size Y (pixels) = 582
	 * First Image is the current Background
	 * Data are written as follows:
	 * FrameNumber	Hour	Minute	Seconds
	 * ============
	 * 
	 * (non-Javadoc)
	 * @see eu.kiaru.ij.controller42.DefaultSynchronizedDisplayedDevice#removeDisplay()
	 */
	
	//CustomWFVirtualStack myVirtualStack;
	ImagePlus myImpPlus;
	//private int currentImageDisplayed;
	//public double avgTimeBetweenImagesInMs;
	//LocalDateTime dateAcquisitionStarted;
	//int numberOfImages;
	
	@Override
	public void makeDisplayVisible() {
		myImpPlus.show();
	}

	@Override
	public void makeDisplayInvisible() {
		myImpPlus.hide();
	}	

	/*@Override
	public void setDisplayedTime(LocalDateTime time) {
		// Needs to find the correct image number
		Duration timeInterval = Duration.between(this.startAcquisitionTime,time);//.dividedBy(numberOfImages-1).toNanos()
		double timeIntervalInMs = (timeInterval.getSeconds()*1000+timeInterval.getNano()/1e6);
		int newImgDisplayed = (int) (timeIntervalInMs/avgTimeBetweenImagesInMs);
		newImgDisplayed+=1;// because of IJ1 notation style
		if (newImgDisplayed<0) {
			newImgDisplayed=0;
		}
		if (newImgDisplayed>numberOfImages) {
			newImgDisplayed=numberOfImages;
		}
		if (newImgDisplayed!=currentImageDisplayed) {
			currentImageDisplayed=newImgDisplayed;
			// needs to update the window, if any
			if (myImpPlus!=null) {
				myImpPlus.setPosition(currentImageDisplayed); // +1 ? // Is this firing an event ?
			}
		}
	}*/
	
	CustomWFVirtualStack42 myVirtualStack;
	@Override
	public void initDevice() {
		// logFile and date of file created already done
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));
		    // Skips header
		    for (int i=0;i<5;i++) {
		    	reader.readLine();
		    }
		    int imgSX=-1;
		    int imgSY=-1;
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
	}

	@Override
	public void imageClosed(ImagePlus src) {
		// TODO Auto-generated method stub
		if (src.getTitle().equals(this.getName())) {
			((CustomWFVirtualStack42) myImpPlus.getStack()).closeFiles();
		}
		
	}

	@Override
	public void imageUpdated(ImagePlus src) {
		// Get the new 
		// Check if the source is correct...
		if (src.getTitle().equals(this.getName())) {
			this.notifyNewSampleDisplayed(src.getCurrentSlice()-1); // because IJ1 notation
		}
	}

	@Override
	public void initDisplay() {
		if (myVirtualStack==null) {System.out.println("prout");}
		myImpPlus = new ImagePlus(this.getName(), myVirtualStack);
		myImpPlus.addImageListener(this);
	}

	@Override
	public void closeDisplay() {
		// TODO Auto-generated method stub
		myImpPlus.close();
	}
	
	// Useless
	@Override
	public void imageOpened(ImagePlus src) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayCurrentSample() {
		// TODO Auto-generated method stub
		if (myImpPlus!=null) {
			myImpPlus.setPosition((int)(this.getCurrentSampleIndexDisplayed())+1); // +1 ? because IJ1 notation
		}
	}

	@Override
	public ImageProcessor getSample(int n) {
		return myImpPlus.getStack().getProcessor(n);
	}
	
	private File logFile;
	private int logVersion;
	
	@Override
	public void initDevice(File f, int vers) {
		// TODO Auto-generated method stub
		logFile=f;	
		logVersion=vers;
		System.out.println("alors on a le logfile");
	}

	

}
