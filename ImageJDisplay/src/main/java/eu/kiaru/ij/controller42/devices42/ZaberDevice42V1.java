package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

public class ZaberDevice42V1 extends ZaberDevice42 {

	@Override
	public void initDevice() {
		System.out.println("ZABER device V1 initialization.");
		// logFile and date of file created already done
		BufferedReader reader;
		ArrayList<ZaberEvent> events = new ArrayList<>();
		// Let's keep all of this in ram
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));			
		    // Skips header
		    for (int i=0;i<5;i++) {
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
		    	System.out.println("nano="+nano);
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
		    	if (evt.eventType.equals("Z moved to")) {
		    		//evt.eventDate.minus(this.startAcquisitionTime.toLocalTime());
		    		Duration timeInterval = Duration.between(this.startAcquisitionTime.toLocalTime(),evt.eventDate);//.dividedBy(numberOfImages-1).toNanos()
					double timeIntervalInMs = ((double)(timeInterval.getSeconds()*1000)+(double)((double)(timeInterval.getNano())/1e6));
					float timeIntervalInS = (float)(timeIntervalInMs/1000.0);
					if (zpos.size()!=0) {
						zpos.add(zpos.get(zpos.size()-1));
						tpos.add(timeIntervalInS);
					}
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

}


