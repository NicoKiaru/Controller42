package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

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
		plotChartZ = new MyPlot();
		plotChartZ.plot = new Plot(this.getName()+"_Z",
				"Time","PositionZ",dataTinS,dataZinMM);		
	}

	@Override
	public void closeDisplay() {
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
		
		BufferedReader reader;
		ArrayList<ZaberEvent> events = new ArrayList<>();
		// Let's keep all of this in ram
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));			
		    // Skips header
		    for (int i=0;i<7;i++) {
		    	reader.readLine();
		    }
		    String line;
		    while ((line = reader.readLine())!=null) {
		    	String[] parts=line.split("\t");
		    	ZaberEvent evt = new ZaberEvent();
		    	int hour = Integer.parseInt(parts[0]);
		    	int minute = Integer.parseInt(parts[1]);
		    	double second = Double.parseDouble(parts[2]);
		    	int nano = (int)((second - ((int)second))*1e9);
		    	//System.out.println("nano="+nano);
		    	LocalTime timeEvt = LocalTime.of(hour, minute, (int) second, nano);
		    	evt.eventDate=timeEvt;
		    	evt.eventType=parts[3];
		    	evt.property=Float.parseFloat(parts[4]);
		    	events.add(evt);
		    }
		    System.out.println(this.startAcquisitionTime);
		    ArrayList<Float> zpos,tpos;
		    zpos = new ArrayList<>();
		    tpos = new ArrayList<>();
		    for (ZaberEvent evt:events) {
		    	if (evt.eventType.equals("Z = ")) {
		    		//evt.eventDate.minus(this.startAcquisitionTime.toLocalTime());
		    		Duration timeInterval = Duration.between(this.startAcquisitionTime.toLocalTime(),evt.eventDate);//.dividedBy(numberOfImages-1).toNanos()
					double timeIntervalInMs = ((double)(timeInterval.getSeconds()*1000)+(double)((double)(timeInterval.getNano())/1e6));
					float timeIntervalInS = (float)(timeIntervalInMs/1000.0);
					/*if (zpos.size()!=0) {
						zpos.add(zpos.get(zpos.size()-1));
						tpos.add(timeIntervalInS);
					}*/
					zpos.add(evt.property);
					tpos.add(timeIntervalInS);
		    	} 
		    }	
		    
		    // beurk .. java
		    dataZinMM = new float[zpos.size()];
		    int i = 0;
		    for (Float f : zpos) {
		    	dataZinMM[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
		    }
		    
		    dataTinS = new float[tpos.size()];
		    i = 0;
		    for (Float f : tpos) {
		    	dataTinS[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
		    }
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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

class ZaberEvent {
	LocalTime eventDate;
	String eventType;
	Float property;
}
