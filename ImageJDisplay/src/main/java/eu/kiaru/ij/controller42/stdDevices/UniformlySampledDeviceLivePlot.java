package eu.kiaru.ij.controller42.stdDevices;

import java.io.File;

import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import ij.gui.Plot;

public class UniformlySampledDeviceLivePlot extends UniformlySampledSynchronizedDisplayedDevice<Double> {
	double[] xdata,ydata;
	public MyPlot plot;
	
	String xLabel;
	String yLabel;
	
	UniformlySampledSynchronizedDisplayedDevice linkedDevice;
	
	
	@Override
	public void displayCurrentSample() {
		long currentSampleDisplayed=this.getCurrentSampleIndexDisplayed();
		double[] range = plot.plot.getLimits(); // xmin xmax ymin ymax
		double width = range[1]-range[0];
		plot.plot.setLimits(currentSampleDisplayed-width/2.0, currentSampleDisplayed+width/2.0, range[2], range[3]);
		plot.checkLineAtCurrentLocation(currentSampleDisplayed);
	}

	@Override
	public Double getSample(int n) {
		return new Double(ydata[n]);
	}

	@Override
	public void initDevice() {
	}
	
	public void initDevice(String name, String xLabel, String yLabel, double[] xdata, double[] ydata) {
		this.setName(name);
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		this.ydata=ydata;
		this.xdata =xdata; 
	}
	
	/*public void setLinkedDevice(UniformlySampledSynchronizedDisplayedDevice device) {
		linkedDevice=device;
		this.copySamplingInfos(linkedDevice);
	}*/

	@Override
	public void initDevice(File f, int version) {
	}

	@Override
	protected void makeDisplayVisible() {
		plot.plot.show();			
	}

	@Override
	public void initDisplay() {
		if (plot==null) plot = new MyPlot();
		plot.plot = new Plot(this.getName(),xLabel,yLabel,xdata,ydata);	
		
	}

	@Override
	public void closeDisplay() {
		plot.plot.getImagePlus().close();	
	}

	@Override
	protected void makeDisplayInvisible() {
		plot.plot.getImagePlus().hide();		
	}

}
