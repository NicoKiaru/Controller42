package eu.kiaru.ij.controller42.devices42;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.ArrayList;

import eu.kiaru.ij.controller42.stdDevices.MyPlot;
import eu.kiaru.ij.controller42.structDevice.SparselySampledSynchronizedDisplayedDevice;
import eu.kiaru.ij.controller42.structDevice.TimedSample;
import ij.gui.Plot;

public class MP285Device42 extends SparselySampledSynchronizedDisplayedDevice<double[]> {

	/*@Override
	public Point3d getSample(LocalDateTime date) {
		// TODO Auto-generated method stub
		return null;
	}*/
	
	MyPlot xPlot, yPlot, zPlot;

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
					TimedSample3DPos currentSample = new TimedSample3DPos();
					currentSample.sample = cpos;
					currentSample.time = Device42Helper.fromMP285LogLine(line);
					this.samples.add(currentSample);					
				}
			});
		   	reader.close();
			this.samplesInitialized();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	File logFile;
	int logVersion;
	@Override
	public void initDevice(File f, int version) {
		System.out.println("File "+f.getName()+" is given, on version "+version+".");
		logVersion=version;
		logFile=f;
	}

	@Override
	protected void makeDisplayVisible() {
		xPlot.plot.getImagePlus().show();
		yPlot.plot.getImagePlus().show();
		zPlot.plot.getImagePlus().show();
	}

	@Override
	public void initDisplay() {
		double[] tSamples = new double[this.samples.size()];
		double[] xPos = new double[this.samples.size()];
		double[] yPos = new double[this.samples.size()];
		double[] zPos = new double[this.samples.size()];
		for (int i=0;i<this.samples.size();i++) {
			tSamples[i] = ((double)(this.samples.get(i).time.toNanoOfDay())/((double)1.0e9)); // in seconds
			xPos[i] = this.samples.get(i).sample[0];
			yPos[i] = this.samples.get(i).sample[1];
			zPos[i] = this.samples.get(i).sample[2];
		}
		
		xPlot =  new MyPlot();
		xPlot.plot = new Plot(this.getName()+"_X","Time","Position",tSamples,xPos);	

		yPlot =  new MyPlot();
		yPlot.plot = new Plot(this.getName()+"_Y","Time","Position",tSamples,yPos);	

		zPlot =  new MyPlot();
		zPlot.plot = new Plot(this.getName()+"_Z","Time","Position",tSamples,zPos);	
			
	}

	@Override
	public void closeDisplay() {
		xPlot.plot.getImagePlus().close();
		yPlot.plot.getImagePlus().close();
		zPlot.plot.getImagePlus().close();
	}

	@Override
	protected void makeDisplayInvisible() {
		xPlot.plot.getImagePlus().hide();
		yPlot.plot.getImagePlus().hide();
		zPlot.plot.getImagePlus().hide();
	}

	@Override
	public void setDisplayedTime(LocalDateTime time) {
		double centerLocation = ((double)(time.toLocalTime().toNanoOfDay())/((double)1.0e9));
		double[] range = xPlot.plot.getLimits(); // xmin xmax ymin ymax
		double width = range[1]-range[0];
		xPlot.plot.setLimits(centerLocation-width/2.0, centerLocation+width/2.0, range[2], range[3]);
		xPlot.checkLineAtCurrentLocation(centerLocation);
		
		range = yPlot.plot.getLimits(); // xmin xmax ymin ymax
		width = range[1]-range[0];
		yPlot.plot.setLimits(centerLocation-width/2.0, centerLocation+width/2.0, range[2], range[3]);
		yPlot.checkLineAtCurrentLocation(centerLocation);
		
		range = zPlot.plot.getLimits(); // xmin xmax ymin ymax
		width = range[1]-range[0];
		zPlot.plot.setLimits(centerLocation-width/2.0, centerLocation+width/2.0, range[2], range[3]);
		zPlot.checkLineAtCurrentLocation(centerLocation);
	}

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