package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import ij.ImagePlus;
import ij.gui.Plot;

public class CamTrackerDevice42 extends DefaultDevice42 {
	
	CameraDevice42 linkedCam;
	public String linkedCamName;
	int currentSampleNumber;
	int currentSampleDisplayed;
	float[][] posData;
	Plot plotChartX,plotChartY;
	
	@Override
	public void initDevice() {
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
				    if (line.startsWith("Linked to")) {
				    	linkedCamName = line.substring("Linked to".length()).trim();
				    }
			      	reader.close();
			      	
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	void setLinkedCamera(CameraDevice42 cam) {
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
		Duration timeInterval = Duration.between(linkedCam.startAcquisitionTime,time);//.dividedBy(numberOfImages-1).toNanos()
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
			double[] range = plotChartX.getLimits(); // xmin xmax ymin ymax
			double width = range[1]-range[0];
			plotChartX.setLimits(newSampledDisplayed-1-width/2.0, newSampledDisplayed-1+width/2.0, range[2], range[3]);
			range = plotChartY.getLimits(); // xmin xmax ymin ymax
			width = range[1]-range[0];
			plotChartY.setLimits(newSampledDisplayed-1-width/2.0, newSampledDisplayed-1+width/2.0, range[2], range[3]);
		}

	}

	@Override
	protected void makeDisplayVisible() {
		// TODO Auto-generated method stub
		plotChartY.show();	
		plotChartX.show();
		
	}

	@Override
	public void initDisplay() {
		System.out.println("initDisplay called in the camtracker");
		// TODO Auto-generated method stub
		plotChartX = new Plot(this.getName()+"_XR",
				"Time","PositionX",posData[0],posData[1]);
		
		System.out.println("posData[0].length="+posData[0].length);
		
		plotChartY = new Plot(this.getName()+"_YR",
				"Time","PositionY",posData[0],posData[2]);
	}

	@Override
	public void closeDisplay() {
		// TODO Auto-generated method stub
		plotChartY.getImagePlus().close();	
		plotChartX.getImagePlus().close();		
	}

	@Override
	protected void makeDisplayInvisible() {
		// TODO Auto-generated method stub
		
	}

}
