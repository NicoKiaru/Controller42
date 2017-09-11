package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import eu.kiaru.ij.controller42.structDevice.MyPlot;
import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import ij.gui.Plot;


public class CamTrackerDevice42 extends UniformlySampledSynchronizedDisplayedDevice<float[]> {
	
	CameraDevice42 linkedCam;
	public String linkedCamName;
	/*int currentSampleNumber;
	int currentSampleDisplayed;*/
	float[][] posData;
	MyPlot plotChartX,plotChartY;
	
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
		    if (line.startsWith("Linked to ")) {
		    	linkedCamName = line.substring("Linked to ".length()).trim();
		    }
		   	reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void skipHeader(BufferedReader reader) throws IOException {
		if (logVersion==2) {
			 for (int i=0;i<10;i++) {
			    	reader.readLine();
			 }
		} 
		if (logVersion==1) {
			for (int i=0;i<8;i++) {
		    	reader.readLine();
			}
		}
	}
	
	void setLinkedCamera(CameraDevice42 cam) {
		System.out.println("SetLinkedCamCalled");
		// logFile and date of file created already done
		BufferedReader reader;
		linkedCam = cam;
		posData = new float[5][cam.getNumberOfSamples()];
		// Let's keep all of this in ram
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));			
		    // Skips header
		    skipHeader(reader);
		    for (int i=0;i<cam.getNumberOfSamples();i++) {
		    	String line = reader.readLine();
		    	String[] parts=line.split("\t");
		    	posData[0][i]=i;
		    	for (int j=1;j<5;j++) {
		    		posData[j][i] = Float.parseFloat(parts[j]);
		    	}
		    }		    
		    this.copySamplingInfos(cam);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*@Override
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
	}*/

	@Override
	protected void makeDisplayVisible() {
		// TODO Auto-generated method stub
		plotChartY.plot.show();	
		plotChartX.plot.show();
		
	}

	@Override
	public void initDisplay() {
		if (plotChartX==null) plotChartX = new MyPlot();
		plotChartX.plot = new Plot(this.getName()+"_XR",
				"Time","PositionX",posData[0],posData[1]);		
		

		if (plotChartY==null) plotChartY = new MyPlot();
		plotChartY.plot = new Plot(this.getName()+"_YR",
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
		plotChartX.plot.getImagePlus().hide();
		plotChartY.plot.getImagePlus().hide();
	}

	File logFile;
	int logVersion;
	@Override
	public void initDevice(File f, int vers) {
		// TODO Auto-generated method stub
		logVersion=vers;
		logFile=f;
	}

	@Override
	public float[] getSample(int n) {
		return new float[] {posData[0][n],posData[1][n],posData[2][n],posData[3][n]};
	}

	@Override
	public void displayCurrentSample() {
		long currentSampleDisplayed=this.getCurrentSampleIndexDisplayed();
		double[] range = plotChartX.plot.getLimits(); // xmin xmax ymin ymax
		double width = range[1]-range[0];
		plotChartX.plot.setLimits(currentSampleDisplayed-width/2.0, currentSampleDisplayed+width/2.0, range[2], range[3]);
		plotChartX.checkLineAtCurrentLocation(currentSampleDisplayed);
		
		range = plotChartY.plot.getLimits(); // xmin xmax ymin ymax
		width = range[1]-range[0];
		plotChartY.plot.setLimits(currentSampleDisplayed-width/2.0, currentSampleDisplayed+width/2.0, range[2], range[3]);
		plotChartY.checkLineAtCurrentLocation(currentSampleDisplayed);		
	}

}
