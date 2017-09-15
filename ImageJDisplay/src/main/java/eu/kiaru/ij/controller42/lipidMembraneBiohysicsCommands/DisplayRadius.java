package eu.kiaru.ij.controller42.lipidMembraneBiohysicsCommands;

import java.time.LocalDateTime;

import org.scijava.command.Command;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

import eu.kiaru.ij.controller42.DSDevicesSynchronizer;
import eu.kiaru.ij.controller42.devices42.CamTrackerDevice42;
import eu.kiaru.ij.controller42.stdDevices.ImagePlusDeviceUniformlySampled;
import eu.kiaru.ij.controller42.stdDevices.LinkedUniformlySampledDeviceLivePlot;
import eu.kiaru.ij.controller42.structTime.TimeIterator;

@Plugin(type = Command.class, menuPath = "Controller 42>Display radius")
public class DisplayRadius implements Command {
	@Parameter
	String synchronizerID;
    
	@Parameter
	String trackerDeviceName = "BEAD_TRACKER";
    
	@Parameter
	String widefieldDeviceName = "CAMERA_GUPPY";
	
	@Parameter
	double kappaInKT;
    
	@Parameter
	int initialFrame;
	
    @Parameter
	int endFrame;
    
    @Parameter
    int stepFrame = 1;
	
	@Parameter
	double x0Trap=0f;
	
	@Parameter
	double y0Trap=0f;
	
	@Parameter
	boolean ignoreYPosition = true;
	
    @Parameter
	double onePixToMicrons = 0.077;
	
    @Parameter
	double xTrapStiffness_pNPerMicron = 79;

    @Parameter
	double yTrapStiffness_pNPerMicron = 40;
    
    @Parameter
    ColorRGB graphColor;
	
    @Parameter
    private ObjectService objService;
    
    @Parameter
    String graphTitle="Radius (nm) vs Time";

	@Override
	public void run() {
		DSDevicesSynchronizer mySync=null;
		for (DSDevicesSynchronizer synchronizer : objService.getObjects(DSDevicesSynchronizer.class)) {
			if (synchronizer.id.equals(synchronizerID)) {
				mySync=synchronizer;
				break;
			}			
		};
		if (mySync==null) {
			System.err.println("Synchronizer id not found!");
			return;
		}
		
		// Looking for devices
		
		ImagePlusDeviceUniformlySampled camDevice = (ImagePlusDeviceUniformlySampled) mySync.getDevices().get(widefieldDeviceName); // only used for timing purpose
		
		CamTrackerDevice42 tracker = (CamTrackerDevice42) mySync.getDevices().get(trackerDeviceName);
		
		// Population checking		
		if (camDevice==null) {
			System.err.println("Camera Device ["+widefieldDeviceName+"] not found.");
			return;
		}
		
		if (tracker==null) {
			System.err.println("Tracker Device ["+trackerDeviceName+"] not found.");
			return;
		}
		
		TimeIterator timeIt = new TimeIterator(camDevice, initialFrame, endFrame, stepFrame);
		
		int numberOfTimeSteps = timeIt.getNumberOfSteps(); 
		double[] tPos = new double[numberOfTimeSteps];
		double[] xPos = new double[numberOfTimeSteps];
		double[] yPos = new double[numberOfTimeSteps];
		double[] forceXInpN = new double[numberOfTimeSteps];
		double[] forceX2InpN2 = new double[numberOfTimeSteps];
		double[] forceYInpN = new double[numberOfTimeSteps];
		double[] forceY2InpN2 = new double[numberOfTimeSteps];
		double[] forceNormInPN = new double[numberOfTimeSteps];
		double[] forceNorm2InPN2 = new double[numberOfTimeSteps];
		double[] forceNorm2InN2 = new double[numberOfTimeSteps];
		double[] tubeRadiusInnm = new double[numberOfTimeSteps];
		
		double kT = 4e-21;
		int index=0;		
	    while (timeIt.hasNext()) {
	    	tPos[index] = index+initialFrame;
	    	
	    	LocalDateTime cTime = timeIt.next();
	    	
	    	xPos[index]=tracker.getSample(cTime)[1];
	    	yPos[index]=tracker.getSample(cTime)[2];
	    	
	    	forceXInpN[index] = (xPos[index]-x0Trap)*onePixToMicrons*xTrapStiffness_pNPerMicron;
	    	forceX2InpN2[index] = forceXInpN[index]*forceXInpN[index];
	    	
	    	if (ignoreYPosition) {
	    		forceYInpN[index] = 0;
		    	forceY2InpN2[index] = 0;
	    	} else {
	    		forceYInpN[index] = (yPos[index]-y0Trap)*onePixToMicrons*yTrapStiffness_pNPerMicron;
		    	forceY2InpN2[index] = forceYInpN[index]*forceYInpN[index];
	    	}    	
	    	
	    	forceNorm2InPN2[index] = forceX2InpN2[index]+forceY2InpN2[index];	
	    	forceNorm2InN2[index] = (forceX2InpN2[index]+forceY2InpN2[index])*1e-24;	 
	    	forceNormInPN[index] = java.lang.Math.sqrt(forceNorm2InPN2[index]);
	    	
	    	tubeRadiusInnm[index] = 2*java.lang.Math.PI*kappaInKT*kT/(forceNormInPN[index]*1e-12)*1e9;
	    	
	    	index++;
	    }
	    
	    LinkedUniformlySampledDeviceLivePlot livePlot = new LinkedUniformlySampledDeviceLivePlot();
	    livePlot.initDevice(graphTitle, "Time", "Radius (nm)", tPos,tubeRadiusInnm);
	    livePlot.setLinkedDevice(camDevice);
	    mySync.addDevice(livePlot);
	    
	    livePlot.showDisplay();
	}
}
