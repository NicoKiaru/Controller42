package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import eu.kiaru.ij.controller42.stdDevices.MyPlot;
import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import ij.gui.Plot;

public class MFCSDevice42 extends UniformlySampledSynchronizedDisplayedDevice<float[]>{
	
    int nMFCSChannels;
    float[][] pressureData;
    float[] indexImage;
	MyPlot[] plotPressure;
    
	@Override
	public void displayCurrentSample() {
		long currentSampleDisplayed=this.getCurrentSampleIndexDisplayed();
		for (int i=0;i<this.nMFCSChannels;i++) {
			double[] range = plotPressure[i].plot.getLimits(); // xmin xmax ymin ymax
			double width = range[1]-range[0];
			plotPressure[i].plot.setLimits(currentSampleDisplayed-width/2.0, currentSampleDisplayed+width/2.0, range[2], range[3]);
			plotPressure[i].checkLineAtCurrentLocation(currentSampleDisplayed);
		}		
		
	}

	@Override
	public float[] getSample(int n) {
		// TODO Auto-generated method stub
		float[] pData = new float[this.nMFCSChannels];
		for (int i=0;i<nMFCSChannels;i++) {
			pData[i] = pressureData[i][n];
		}
		return pData;
	}

	
	@Override
	public void initDevice() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));
		    // Skips header
		    for (int i=0;i<6;i++) {
		    	reader.readLine();
		    }
		    
		    String firstLine=reader.readLine();

		    if (firstLine==null) {
		    	System.err.println("Could not find proper information... in device "+this.getName());
				
		    	reader.close();
		    	return;
		    } else {
		    	String[] parts=firstLine.split("\t");
			    nMFCSChannels = parts.length-4; //removes hour min sec PA= columns
		    }
		    String lastLine = "";
		    String sCurrentLine;
		    int nSamples=1;
		    while ((sCurrentLine = reader.readLine()) != null) 
		    {
		        lastLine = sCurrentLine;
		        nSamples++;
		    }
		    LocalTime timeIni = Device42Helper.fromMFCSLogLine(firstLine);//.LocalTime.parse(firstLine,formatter);			    
		    LocalTime timeEnd = null;
		   	double avgTimeBetweenImagesInMs = 1;
		   	if (!lastLine.equals("")) {
			   	timeEnd = Device42Helper.fromMFCSLogLine(lastLine);			   
			   	avgTimeBetweenImagesInMs = Duration.between(timeIni,timeEnd).dividedBy(nSamples-1).toNanos()/1e6;
		   	}
		   	startAcquisitionTime=LocalDateTime.of(this.startAcquisitionTime.toLocalDate(),timeIni);
		   	endAcquisitionTime=LocalDateTime.of(this.startAcquisitionTime.toLocalDate(),timeEnd).plus(Duration.ofNanos((long)(avgTimeBetweenImagesInMs*1e6)));
	      	
	    	this.setSamplingInfos(startAcquisitionTime, endAcquisitionTime, nSamples);
		   	reader.close();
	      	
		   	// Now get the sample values
		   	reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));
		    // Skips header
		    for (int i=0;i<6;i++) {
		    	reader.readLine();
		    }

			pressureData = new float[nMFCSChannels][nSamples];
		    indexImage = new float[nSamples];
		    for (int i=0;i<nSamples;i++) {
		    	indexImage[i]=i;
		    }
			nSamples=0;
		    while ((sCurrentLine = reader.readLine()) != null) 
		    {
		        String[] parts=sCurrentLine.split("\t");
				for (int i=4;i<parts.length;i++) {
					pressureData[i-4][nSamples]= Float.parseFloat(parts[i]);
				}
		        nSamples++;
		    }
		    
		    reader.close();
		   	
	      	// now Fetch data and open them
	      			    
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private File logFile;
	private int logVersion;
	
	@Override
	public void initDevice(File f, int version) {
		logFile=f;	
		logVersion=version;		
	}

	@Override
	protected void makeDisplayVisible() {
		for (int i=0;i<this.nMFCSChannels;i++) {
			plotPressure[i].plot.show();
		}		
	}

	@Override
	public void initDisplay() {
		plotPressure =  new MyPlot[this.nMFCSChannels];
		for (int i=0;i<this.nMFCSChannels;i++) {
			plotPressure[i] = new MyPlot();
			plotPressure[i].plot = new Plot(this.getName()+"_Ch"+i,
					"Time","Pressure",indexImage,pressureData[i]);	
		}		
	}

	@Override
	public void closeDisplay() {		
		for (int i=0;i<this.nMFCSChannels;i++) {
			plotPressure[i].plot.getImagePlus().close();
		}
	}

	@Override
	protected void makeDisplayInvisible() {
		for (int i=0;i<this.nMFCSChannels;i++) {
			plotPressure[i].plot.getImagePlus().hide();
		}		
	}

}
