package eu.kiaru.ij.controller42.opticalTrapCalibration;

import java.time.LocalDateTime;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import eu.kiaru.ij.controller42.DSDevicesSynchronizer;
import eu.kiaru.ij.controller42.devices42.CamTrackerDevice42;
import eu.kiaru.ij.controller42.devices42.MP285Device42;
import eu.kiaru.ij.controller42.stdDevices.ImagePlusDeviceUniformlySampled;
import eu.kiaru.ij.controller42.stdDevices.UniformlySampledDeviceLivePlot;
import eu.kiaru.ij.controller42.structTime.UniformTimeIterator;
import ij.gui.Plot;
import ij.measure.CurveFitter;

@Plugin(type = Command.class, menuPath = "Controller 42>Optical Trap Calibrate")
public class ComputeStiffness implements Command {
	
	@Parameter
	DSDevicesSynchronizer synchronizer;
	
	@Parameter
	String mp285DeviceName = "MP285_LEFT";
    
	@Parameter
	String trackerDeviceName = "BEAD_TRACKER";
    
	@Parameter
	String sampleLikeDeviceName = "CAMERA_GUPPY";
    
	@Parameter
	int initialFrame;
	
    @Parameter
	int endFrame;
    
    @Parameter
    int stepFrame = 1;
	
    @Parameter
    double beadDiameterInMicrons = 3;
    
    @Parameter
    double dynamicViscosity = 1e-3;
    
    @Parameter
	double onePixToMicrons = 0.077;
	
    @Parameter
    private ObjectService objService;
    
    @Parameter
    private UIService uiService;
    
    @Parameter
    private LogService lService;
    
    @Parameter
    String graphTitle="Bead displacement (um) vs Speed ( um.s-1 )";
	
	@Override
	public void run() {
		
		// Looking for devices		
		MP285Device42 mp = (MP285Device42) synchronizer.getDevices().get(mp285DeviceName);
		
		ImagePlusDeviceUniformlySampled camDevice = (ImagePlusDeviceUniformlySampled) synchronizer.getDevices().get(sampleLikeDeviceName); // only used for timing purpose
		
		CamTrackerDevice42 tracker = (CamTrackerDevice42) synchronizer.getDevices().get(trackerDeviceName);
		
		// Population checking
		if (mp==null) {
			System.err.println("Zaber Device ["+mp285DeviceName+"] not found.");
			return;
		}
		
		if (camDevice==null) {
			System.err.println("Camera Device ["+sampleLikeDeviceName+"] not found.");
			return;
		}
		
		if (tracker==null) {
			System.err.println("Tracker Device ["+trackerDeviceName+"] not found.");
			return;
		}
		
		UniformTimeIterator timeIt = new UniformTimeIterator(camDevice, initialFrame, endFrame, stepFrame);
		
		int numberOfTimeSteps = timeIt.getNumberOfSteps();
		double[] xPosBead = new double[numberOfTimeSteps];
		double[] yPosBead = new double[numberOfTimeSteps];
		
		double[] xPosMP = new double[numberOfTimeSteps];
		double[] yPosMP = new double[numberOfTimeSteps];
		
		double[] xSpeedMP = new double[numberOfTimeSteps];
		double[] ySpeedMP = new double[numberOfTimeSteps];
		double[] tPos = new double[numberOfTimeSteps];
		
		int index=0;
		
	    while (timeIt.hasNext()) {
	    	LocalDateTime cTime = timeIt.next();
	    	tPos[index] = index;

	    	xPosBead[index]=tracker.getSample(cTime)[1]*this.onePixToMicrons;
	    	yPosBead[index]=tracker.getSample(cTime)[2]*this.onePixToMicrons;
	    	
	    	xPosMP[index]=mp.getSample(cTime)[0];
	    	yPosMP[index]=mp.getSample(cTime)[1];
	    	
	    	if ((index>1)) {
	    		xSpeedMP[index]=(xPosMP[index]-xPosMP[index-1])/(timeIt.getDurationBetweenSamplesInMs()/1000);// in um/s
	    		ySpeedMP[index]=(yPosMP[index]-yPosMP[index-1])/(timeIt.getDurationBetweenSamplesInMs()/1000);	    		
	    	}
	    	
	    	index++;
	    }
	    
	    Plot xspeedPlot = new Plot("x z plot","XPOS (um)","MP 285 Speed (um.s-1)");
	    xspeedPlot.addPoints(xPosBead, xSpeedMP, Plot.CROSS);
	    
	    // Display MP285 speed X vs number of image
	    UniformlySampledDeviceLivePlot livePlotSpeedMP285 = new UniformlySampledDeviceLivePlot();
	    livePlotSpeedMP285.initDevice(graphTitle, "Time", "MP285SpeedX(um.s-1)", tPos,xSpeedMP);
	    livePlotSpeedMP285.setSamplingInfos(timeIt.startTime, timeIt.endTime, timeIt.getNumberOfSteps());
	    synchronizer.addDevice(livePlotSpeedMP285);	    
	    livePlotSpeedMP285.showDisplay();
	    
	    	        
	    CurveFitter cf = new CurveFitter(xPosBead, xSpeedMP);
	    cf.doFit(CurveFitter.STRAIGHT_LINE);
	    
	    double[] xFit = new double[2];
	    double[] yFit = new double[2];
	    
	    double minDisplacement = Double.MAX_VALUE;
	    double maxDisplacement = -Double.MAX_VALUE;
	    
	    for (int i=0;i<numberOfTimeSteps;i++) {
	    	if (xPosBead[i]>maxDisplacement) {
	    		maxDisplacement = xPosBead[i];
	    	}
	    	if (xPosBead[i]<minDisplacement) {
	    		minDisplacement = xPosBead[i];
	    	}
	    	
	    }
	    xFit[0] = minDisplacement;
	    xFit[1] = maxDisplacement;
	    
	    yFit[0] = cf.f(xFit[0]);
	    yFit[1] = cf.f(xFit[1]);	   
	    
	    xspeedPlot.addPoints(xFit, yFit, Plot.LINE);
	    
	    xspeedPlot.show();
	    
	    System.out.println("------ Fit Displacement (um) vs Speed (um.s-1) ---------"); 
	    
	    System.out.println("R^2="+cf.getRSquared());
	    System.out.println("Slope="+cf.getParams()[1]);
	    
	    double factorSpeedToForceInpN = 6*java.lang.Math.PI*this.dynamicViscosity*this.beadDiameterInMicrons/2.0*1e-6*1e-6*1e12; // result in pN
	    
	    double stiffness = factorSpeedToForceInpN*cf.getParams()[1];
	    
	    String report="------------\n";
	    report+="Bead Calibration of experiment:"+synchronizer.id+"\n";
	    report+="Viscosity = "+this.dynamicViscosity+" Pa.s.\n";
	    report+="Bead radius = "+(this.beadDiameterInMicrons/2.0)+" um.\n";
	    report+="One pix = "+this.onePixToMicrons+" um.\n";
	    report+="Stiffness = "+stiffness+" pN.um-1.\n";
	    
	    uiService.show(report);
	    lService.info(report);
	    
	}

}
