package eu.kiaru.ij.controller42.devices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import ij.ImagePlus;

public class CamTrackerDevice extends Controller42Device {
	
	CameraDevice linkedCam;
	public String linkedCamName;
	int currentSampleNumber;
	int currentSampleDisplayed;
	float[][] posData;
	LiveLineChartIJ1 plotChartX,plotChartY;
	
	@Override
	void init42Device() {
		// logFile and date of file created already done
				BufferedReader reader;
				try {
					reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));
					
				    // Skips header
				    for (int i=0;i<4;i++) {
				    	reader.readLine();
				    }
				    int imgSX=-1;
				    int imgSY=-1;
				    String line = reader.readLine(); // image size X
				    if (line.startsWith("Linked To =")) {
				    	linkedCamName = line.substring("Linked To =".length()).trim();
				    }
			      	reader.close();
			      	
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	void setLinkedCamera(CameraDevice cam) {
		System.out.println("SetLinkedCamCalled");
		// logFile and date of file created already done
		BufferedReader reader;
		linkedCam = cam;
		posData = new float[5][cam.numberOfImages];
		// Let's keep all of this in ram
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));			
		    // Skips header
		    for (int i=0;i<10;i++) {
		    	reader.readLine();
		    }
		    for (int i=0;i<cam.numberOfImages;i++) {
		    	String line = reader.readLine();
		    	String[] parts=line.split("\t");
		    	posData[0][i]=i;
		    	for (int j=1;j<5;j++) {
		    		posData[j][i] = Float.parseFloat(parts[j]);
		    	}
		    }		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.showDisplay();
	}

	@Override
	public void setDisplayedTime(LocalDateTime time) {		
		// TODO Auto-generated method stub
		Duration timeInterval = Duration.between(linkedCam.dateAcquisitionStarted,time);//.dividedBy(numberOfImages-1).toNanos()
		double timeIntervalInMs = (timeInterval.getSeconds()*1000+timeInterval.getNano()/1e6);
		int newSampledDisplayed = (int) (timeIntervalInMs/linkedCam.avgTimeBetweenImagesInMs);
		if (newSampledDisplayed<0) {
			newSampledDisplayed=0;
		}
		newSampledDisplayed+=1; // because IJ1 notation
		if (newSampledDisplayed>linkedCam.numberOfImages) {
			newSampledDisplayed=linkedCam.numberOfImages;
		}
		if (newSampledDisplayed!=currentSampleDisplayed) {
			currentSampleDisplayed=newSampledDisplayed;
			double[] range = plotChartX.plot.getLimits(); // xmin xmax ymin ymax
			double width = range[1]-range[0];
			plotChartX.plot.setLimits(newSampledDisplayed-1-width/2.0, newSampledDisplayed-1+width/2.0, range[2], range[3]);
			range = plotChartY.plot.getLimits(); // xmin xmax ymin ymax
			width = range[1]-range[0];
			plotChartY.plot.setLimits(newSampledDisplayed-1-width/2.0, newSampledDisplayed-1+width/2.0, range[2], range[3]);
		}

	}

	@Override
	protected void makeDisplayVisible() {
		// TODO Auto-generated method stub
		plotChartY.plot.show();	
		plotChartX.plot.show();
		
	}

	@Override
	public void initDisplay() {
		System.out.println("initDisplay called in the camtracker");
		// TODO Auto-generated method stub
		plotChartX = new LiveLineChartIJ1(this.getName()+"_XR",
				"Time","PositionX",posData[0],posData[1]);
		
		System.out.println("posData[0].length="+posData[0].length);
		
		plotChartY = new LiveLineChartIJ1(this.getName()+"_YR",
				"Time","PositionY",posData[0],posData[2]);
	}

	@Override
	public void closeDisplay() {
		// TODO Auto-generated method stub
		plotChartY.plot.getImagePlus().close();	
		plotChartX.plot.getImagePlus().close();		
	}

	@Override
	protected void makeDisplayInvisible() {
		// TODO Auto-generated method stub
		
	}

}
