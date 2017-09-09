package eu.kiaru.ij.controller42.devices42;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import eu.kiaru.ij.controller42.structDevice.MyPlot;
import ij.gui.Plot;

public class ZaberDevice42 extends DefaultSynchronizedDisplayedDevice {
	float[] dataZinMM;
	float[] dataTinS; // relative to startacquisition
	MyPlot plotChartZ;
	
	@Override
	public void setDisplayedTime(LocalDateTime time) {
		// TODO Auto-generated method stub
		Duration timeInterval = Duration.between(this.startAcquisitionTime.toLocalTime(),time);//.dividedBy(numberOfImages-1).toNanos()
		double timeIntervalInMs = ((double)(timeInterval.getSeconds()*1000)+(double)((double)(timeInterval.getNano())/1e6));
		double timeIntervalInS = (float)(timeIntervalInMs/1000.0);
		
		double[] range = plotChartZ.plot.getLimits(); // xmin xmax ymin ymax
		double width = range[1]-range[0];
		plotChartZ.plot.setLimits(timeIntervalInS-width/2.0, timeIntervalInS+width/2.0, range[2], range[3]);
		plotChartZ.checkLineAtCurrentLocation(timeIntervalInS);
	}

	@Override
	protected void makeDisplayVisible() {
		// TODO Auto-generated method stub
		plotChartZ.plot.show();		
	}

	@Override
	public void initDisplay() {
		System.out.println("initDisplay called in ZaberDevice");
		// TODO Auto-generated method stub
		plotChartZ = new MyPlot();
		plotChartZ.plot = new Plot(this.getName()+"_Z",
				"Time","PositionZ",dataTinS,dataZinMM);		
	}

	@Override
	public void closeDisplay() {
		// TODO Auto-generated method stub

		plotChartZ.plot.getImagePlus().close();	
		
	}

	@Override
	protected void makeDisplayInvisible() {
		// TODO Auto-generated method stub
		plotChartZ.plot.getImagePlus().hide();
		
	}

	@Override
	public void initDevice() {
		System.out.println("ZABER device initialization.");
		/*System.out.println("SetLinkedCamCalled");
		// logFile and date of file created already done
		BufferedReader reader;
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
		}*/
		
	}

	File logFile;
	int logVersion;
	
	@Override
	public void initDevice(File f,int vers) {
		// TODO Auto-generated method stub
		logFile=f;
		logVersion=vers;
		
	}

}
