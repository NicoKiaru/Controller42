package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import eu.kiaru.ij.controller42.structDevice.SparselySampledSynchronizedDisplayedDevice;
import eu.kiaru.ij.controller42.structDevice.TimedSample;

public class MP285Device42 extends SparselySampledSynchronizedDisplayedDevice<double[]> {

	/*@Override
	public Point3d getSample(LocalDateTime date) {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public void initDevice() {
		// TODO Auto-generated method stub
		System.out.println("Initializing MP285 device named "+this.getName());
		// logFile and date of file created already done
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(this.logFile.getAbsolutePath()));
			// skips header
			for (int i=0;i<6;i++) {
		    	reader.readLine();
		    }
			
			this.samples = new ArrayList<TimedSample<double[]>>();
			
			reader.lines().forEachOrdered(line -> {
				String[] parts=line.split("\t");
				if (parts[3].equals("P measured")) {
					// real data point
					double[] cpos = new double[3];
					cpos[0] = Double.parseDouble(parts[4]);
					cpos[1] = Double.parseDouble(parts[5]);
					cpos[2] = Double.parseDouble(parts[6]);
					//System.out.println("["+cpos[0]+","+cpos[1]+","+cpos[2]+"]");
					TimedSample3DPos currentSample = new TimedSample3DPos();
					currentSample.sample = cpos;
					currentSample.time = Device42Helper.fromMP285LogLine(line);
					//System.out.println(currentSample.time);
					this.samples.add(currentSample);
					
				}
		    	/*posData[0][i]=i;
		    	for (int j=1;j<5;j++) {
		    		posData[j][i] = Float.parseFloat(parts[j]);
		    	}*/
				
				/*
				 * String line = reader.readLine();
		    	String[] parts=line.split("\t");
		    	posData[0][i]=i;
		    	for (int j=1;j<5;j++) {
		    		posData[j][i] = Float.parseFloat(parts[j]);
		    	}
				 */
			});
		   	reader.close();
			this.samplesInitialized();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	File logFile;
	int logVersion;
	@Override
	public void initDevice(File f, int version) {
		// TODO Auto-generated method stub
		System.out.println("File "+f.getName()+" is given, on version "+version+".");
		// TODO Auto-generated method stub
		logVersion=version;
		logFile=f;
	}

	@Override
	protected void makeDisplayVisible() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void makeDisplayInvisible() {
		// TODO Auto-generated method stub
		
	}

	/*@Override
	public void setDisplayedTime(LocalDateTime time) {
		// TODO Auto-generated method stub
		
	}*/

}

class TimedSample3DPos extends TimedSample<double[]>{

	@Override
	public double[] interpolate(double[] ti, double[] tf, double ratio_I_to_F) {
		// TODO Auto-generated method stub
		double[] ans = new double[3];
		ans[0]=(float)(ti[0]+ratio_I_to_F*(tf[0]-ti[0]));

		ans[1]=(float)(ti[1]+ratio_I_to_F*(tf[1]-ti[1]));

		ans[2]=(float)(ti[2]+ratio_I_to_F*(tf[2]-ti[2]));
		return ans;
	}
	
}