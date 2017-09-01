package eu.kiaru.ij.controller42.devices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import ij.*;

import eu.kiaru.ij.controller42.DefaultSynchronizedDisplayedDevice;

public class CameraDevice extends Controller42Device implements ImageListener{
	/* Typical Header:
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
	private int currentImageDisplayed;
	public double avgTimeBetweenImagesInMs;
	LocalDateTime dateAcquisitionStarted;
	int numberOfImages;
	
	@Override
	protected void removeDisplay() {		
	}

	@Override
	protected void showDisplay() {		
	}	

	@Override
	public void setDisplayedTime(LocalDateTime time) {
		// Needs to find the correct image number
		Duration timeInterval = Duration.between(dateAcquisitionStarted,time);//.dividedBy(numberOfImages-1).toNanos()
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
	}
	
	@Override
	void init42Device() {
		// logFile and date of file created already done
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));
			
		    // Skips header
		    for (int i=0;i<6;i++) {
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
		    
		    System.out.println(imgSX+":"+imgSY);
		    
		    String firstLine=reader.readLine();
		    if (firstLine==null) {
		    	System.err.println("Could not find proper camera information... in device "+this.getName());
		    	reader.close();
		    	return;
		    }
		    System.out.println(firstLine);
		    String lastLine = "";
		    String sCurrentLine;
		    numberOfImages=1;
		    while ((sCurrentLine = reader.readLine()) != null) 
		    {
		    	//System.out.println(sCurrentLine);
		        lastLine = sCurrentLine;
		        numberOfImages++;
		    }
		    //DateFormat format = new SimpleDateFormat("'1\t'HH'\t'mm'\t'ss.SSSS", Locale.FRANCE);
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'1\t'HH'\t'mm'\t'ss.SSSS");
		    LocalTime timeIni = LocalTime.parse(firstLine,formatter);		   	
		    
		    
		    LocalTime timeEnd = null;
		   	avgTimeBetweenImagesInMs = 1;
		   	if (!lastLine.equals("")) {
		   		lastLine = lastLine.substring(lastLine.indexOf('\t')+1);
		   		formatter = DateTimeFormatter.ofPattern("HH'\t'mm'\t'ss.SSSS");
			   	timeEnd = LocalTime.parse(lastLine,formatter);
			   	avgTimeBetweenImagesInMs = Duration.between(timeIni,timeEnd).dividedBy(numberOfImages-1).toNanos()/1e6;
			   	if (avgTimeBetweenImagesInMs<0) {
			   		System.err.println("Negative time between images... Are you acquiring overnight ?");
			   		reader.close();
			   		return; // I hope there's no overnight acquisition
			   	}
		   	} 
		   	dateAcquisitionStarted=LocalDateTime.of(this.dateOfFileCreated.toLocalDate(),timeIni);
	      	reader.close();
	      	
	      	System.out.println("dateAcquisitionStarted="+dateAcquisitionStarted);
	      	System.out.println("avgTimeBetweenImagesInMs="+avgTimeBetweenImagesInMs);
	      	
	      	// now Fetch data and open them
	      	String attachedRawDataPrefixFile = this.logFile.getPath().substring(0, this.logFile.getPath().length()-4);

	      	CustomWFVirtualStack myVirtualStack = new CustomWFVirtualStack(imgSX, imgSY, numberOfImages, null, null);
	      	myVirtualStack.setAttachedDataPath(attachedRawDataPrefixFile);
			myImpPlus = new ImagePlus(this.getName(), myVirtualStack);
			myImpPlus.show();
			myImpPlus.addImageListener(this);
	      	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}

	@Override
	public void imageClosed(ImagePlus src) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageOpened(ImagePlus src) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageUpdated(ImagePlus src) {
		// TODO Auto-generated method stub
		int newDisplayedSlice = src.getCurrentSlice();
		// Get the new 
		// Check if the source is correct...
		if (src.getTitle().equals(this.getName())) {
			if (newDisplayedSlice==this.currentImageDisplayed) {
				// do nothing
			} else {
				// Needs to compute the Instant on which this image was Acquired
				double durationInNS = avgTimeBetweenImagesInMs*(newDisplayedSlice-1)*1e6+avgTimeBetweenImagesInMs/2.0;
				this.setCurrentTime(this.dateAcquisitionStarted.plusNanos((long)durationInNS));			
				this.currentImageDisplayed=newDisplayedSlice;
				this.fireDeviceTimeChangedEvent();
			}
		}
	}
}
