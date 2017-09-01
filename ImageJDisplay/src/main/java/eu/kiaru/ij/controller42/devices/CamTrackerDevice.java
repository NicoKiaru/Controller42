package eu.kiaru.ij.controller42.devices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;

public class CamTrackerDevice extends Controller42Device {
	
	CameraDevice linkedCam;
	int currentSampleNumber;
	int currentSampleDisplayed;
	float[][] posData;
	LiveLineChartIJ1 plotChartX,plotChartY;
	
	@Override
	void init42Device() {
		// TODO Auto-generated method stub
		
	}

	void setLinkedCamera(CameraDevice cam) {
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
		plotChartX = new LiveLineChartIJ1(this.getName()+"_XR",
				"Time","PositionX",posData[0],posData[1]);

		plotChartX.plot.show();
		plotChartY = new LiveLineChartIJ1(this.getName()+"_YR",
				"Time","PositionY",posData[0],posData[2]);

		plotChartY.plot.show();
		this.isDisplayed=true;		
	}

	@Override
	protected void removeDisplay() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void showDisplay() {
		// TODO Auto-generated method stub

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

}
