package eu.kiaru.ij.controller42.opticalTrapCalibration;

import java.awt.Color;
import java.awt.Window;
import java.time.LocalDateTime;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.util.ColorRGB;

import eu.kiaru.ij.controller42.DSDevicesSynchronizer;
import eu.kiaru.ij.controller42.devices42.CamTrackerDevice42;
import eu.kiaru.ij.controller42.devices42.MP285Device42;
import eu.kiaru.ij.controller42.devices42.ZaberDevice42;
import eu.kiaru.ij.controller42.stdDevices.ImagePlusDeviceUniformlySampled;
import eu.kiaru.ij.controller42.structTime.UniformTimeIterator;
import ij.WindowManager;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.measure.CurveFitter;

@Plugin(type = Command.class, menuPath = "Controller 42>Optical Trap Calibrate")
public class ComputeStiffness implements Command {
	
	//@Parameter
	//String synchronizerID;
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
	
	/*@Parameter
	double x0Trap=0f;
	
	@Parameter
	double y0Trap=0f;*/
	
	/*@Parameter
	boolean ignoreYPosition = true;*/
	
    @Parameter
    double beadDiameterInMicrons = 3;
    
    @Parameter
    double dynamicViscosity = 1e-3;
    
    @Parameter
	double onePixToMicrons = 0.077;
	
    @Parameter
    ColorRGB fitColor;
    
    @Parameter
    ColorRGB dataColor;
    
    /*@Parameter
    boolean appendToExistingGraph;*/
	
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
		/*DSDevicesSynchronizer mySync=null;
		for (DSDevicesSynchronizer synchronizer : objService.getObjects(DSDevicesSynchronizer.class)) {
			if (synchronizer.id.equals(synchronizerID)) {
				mySync=synchronizer;
				break;
			}			
		};
		if (mySync==null) {
			System.err.println("Synchronizer id not found!");
			return;
		}*/
		
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
		//double[] zPos = new double[numberOfTimeSteps];
		double[] xPosBead = new double[numberOfTimeSteps];
		double[] yPosBead = new double[numberOfTimeSteps];
		
		double[] xPosMP = new double[numberOfTimeSteps];
		double[] yPosMP = new double[numberOfTimeSteps];
		
		double[] xSpeedMP = new double[numberOfTimeSteps];
		double[] ySpeedMP = new double[numberOfTimeSteps];
		
		/*double[] forceXInpN = new double[numberOfTimeSteps];
		double[] forceX2InpN2 = new double[numberOfTimeSteps];
		double[] forceYInpN = new double[numberOfTimeSteps];
		double[] forceY2InpN2 = new double[numberOfTimeSteps];
		double[] forceNormInPN = new double[numberOfTimeSteps];
		double[] forceNorm2InPN2 = new double[numberOfTimeSteps];
		double[] forceNorm2InN2 = new double[numberOfTimeSteps];
		double[] mbTensionInNperM = new double[numberOfTimeSteps];*/
		
		int index=0;
		
		//double pascalToMbTensionScaleFactor = 1/2f*(1/(1/pipetteRadiusInMicrons-1/vesicleRadiusInMicrons))*1e-6; // because microns...
		
	    while (timeIt.hasNext()) {
	    	LocalDateTime cTime = timeIt.next();
	    	
	    	//zPos[index]=zaber.getSample(cTime);
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
	    System.out.println("Viscosity = "+this.dynamicViscosity+" Pa.s.");
	    System.out.println("Bead radius = "+(this.beadDiameterInMicrons/2.0)+" um.");
	    System.out.println("Stiffness = "+stiffness+" pN.um-1.");
	    
	    
	    //System.out.println("Origin="+cf.getParams()[0]);
	   // double kT = 4e-21; // because pN
	   // double kappa = cf.getParams()[1]/(8*java.lang.Math.PI*java.lang.Math.PI)/kT;
	   // double sigma0 = cf.getParams()[0]/cf.getParams()[1];
	    
	    
	   
	    //
	    //kappa=p(1)/(8*pi*pi)/kT; % in kT
	    //disp(['kappa = ',num2str(kappa),' kT']);
	  /*  String report = "";
	    report+=" - Kappa measurement report \n";
	    report+="--------------------------\n";
	    report+="Input : \n";
	    report+="sync ID = "+synchronizer.id+"\n";
	    report+="Zaber  = "+zaberDeviceName+"\n";
	    report+="\t zPos conversion 1 unit = "+zPositionToPaConversionFactor+" Pa\n";
	    report+="Tracker = "+trackerDeviceName+"\n";
	    report+="Camera = "+sampleLikeDeviceName+"\n";
	    report+=" 1 pix = "+this.onePixToMicrons+" um\n";
	    report+="\t TP ini   = "+initialFrame+"\n";
	    report+="\t TP end   = "+endFrame+"\n";
	    report+="\t TP step  = "+stepFrame+"\n";
	    report+="Radius Pipette = "+this.pipetteRadiusInMicrons+" um\n";
	    report+="Radius Vesicle = "+this.vesicleRadiusInMicrons+" um\n";
	    report+="Trap \n";
	    report+="\t xOTrap = "+this.x0Trap+" (px)\n";
	    report+="\t X stiffness = "+this.xTrapStiffness_pNPerMicron+" pN.um-1 \n";
	    if (this.ignoreYPosition) {
	    	report+="Ignore Y Axis\n";
	    } else {
	    	report+="\t yOTrap = "+this.y0Trap+" (px)\n";
		    report+="\t Y stiffness = "+this.yTrapStiffness_pNPerMicron+" pN.um-1 \n";
	    }
	    
	    report+="--------------------------\n";
	    report+="Output:\n";
	    report+="\t Kappa  = "+kappa+" kT\n";
	    report+="\t R^2  = "+cf.getRSquared()+"\n";
	    
	    uiService.show(report);
	    lService.info(report);
	    
	    Plot f2sigmaPlot;
	    if (this.appendToExistingGraph) {
	    	// look for existing previous graph
	    	Window existingGraphWindow = WindowManager.getWindow(graphTitle);	    	
	    	if (existingGraphWindow==null) {
	    		System.err.println("Couldn't find preexisting "+graphTitle+" plot.");
	    		f2sigmaPlot = new Plot(graphTitle,"Sigma (N.m-1)","Force^2(pN^2)");
	    	} else {
		    	if (existingGraphWindow.getClass().equals(PlotWindow.class)) {
		    		f2sigmaPlot = ((PlotWindow) existingGraphWindow).getPlot();
		    	} else {
		    		System.err.println("Couldn't find preexisting "+graphTitle+" plot.");
		    		f2sigmaPlot = new Plot(graphTitle,"Sigma (N.m-1)","Force^2(pN^2)");
		    	}
	    	}	
	    } else {
	    	f2sigmaPlot = new Plot(graphTitle,"Sigma (N.m-1)","Force^2(pN^2)");//	
	    }
	      
	    for (int i=0;i<numberOfTimeSteps;i++) {
	    	mbTensionInNperM[i]+=sigma0;
	    }
	    xFit[0]+=sigma0;
	    xFit[1]+=sigma0;
	    yFit[0]*=1e24;
	    yFit[1]*=1e24;
	    */
	    
	    /*f2sigmaPlot = new Plot(graphTitle,"Sigma (N.m-1)","Force^2(pN^2)");

	    f2sigmaPlot.setColor(new Color(dataColor.getRed(),dataColor.getBlue(),dataColor.getGreen()));
	    f2sigmaPlot.addPoints(mbTensionInNperM, forceNorm2InPN2, Plot.CROSS);
	    f2sigmaPlot.setColor(new Color(fitColor.getRed(),fitColor.getBlue(),fitColor.getGreen()));
	    f2sigmaPlot.addPoints(xFit, yFit, Plot.LINE);
	    f2sigmaPlot.show();*/
	}

}
