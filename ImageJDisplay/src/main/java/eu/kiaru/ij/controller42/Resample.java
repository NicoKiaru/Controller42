package eu.kiaru.ij.controller42;

import java.time.LocalDateTime;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import eu.kiaru.ij.controller42.stdDevices.ImagePlusDeviceUniformlySampled;
import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import eu.kiaru.ij.controller42.structTime.UniformTimeIterator;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

@Plugin(type = Command.class, menuPath = "Controller 42>Resample device as image stack")
public class Resample implements Command{
	@Parameter
	DSDevicesSynchronizer synchronizer;
	
	@Parameter
	String deviceToResample;
	
	@Parameter
	String samplerDeviceName;
	
	@Parameter
	int initialFrame;
	
    @Parameter
	int endFrame;
    
    @Parameter
    int stepFrame = 1;
	
	@Override
	public void run() {
		UniformlySampledSynchronizedDisplayedDevice samplerRefDevice = (UniformlySampledSynchronizedDisplayedDevice) synchronizer.getDevices().get(samplerDeviceName); // only used for timing purpose
		
		
		// Population checking		
		if (samplerRefDevice==null) {
			System.err.println("Device ["+samplerDeviceName+"] not found.");
			return;
		}
				
		UniformTimeIterator timeIt = new UniformTimeIterator(samplerRefDevice, initialFrame, endFrame, stepFrame);
		
		DefaultSynchronizedDisplayedDevice dsdd = synchronizer.getDevices().get(deviceToResample);
		
		if (dsdd==null) {
			System.err.println("Device ["+deviceToResample+"] not found.");
			return;
		}
		
		// Getting class of device ...
		//while (timeIt.hasNext()) {
	    LocalDateTime cTime = timeIt.next();
	    Object sample = dsdd.getSample(cTime);
	    System.out.println(sample.getClass());
	    if (ImageProcessor.class.isAssignableFrom(sample.getClass())) {	
	    	ImageProcessor imgP = (ImageProcessor) sample;
	    	System.out.println("You want to resample an image");
	    	ImagePlusDeviceUniformlySampled imgDevice = new ImagePlusDeviceUniformlySampled();
	    	imgDevice.setName("Resampled-"+dsdd.getName());
	    	imgDevice.setSamplingInfos(timeIt.startTime, timeIt.endTime, timeIt.getNumberOfSteps());	
	    	ImageStack is = new ImageStack(imgP.getWidth(),imgP.getHeight());
	    	timeIt = new UniformTimeIterator(samplerRefDevice, initialFrame, endFrame, stepFrame);
	    	while (timeIt.hasNext()) {
	 	    	cTime = timeIt.next();
	 	    	is.addSlice((ImageProcessor)((ImageProcessor)dsdd.getSample(cTime)).clone());
	    	}	    	
	    	imgDevice.myImpPlus = new ImagePlus(imgDevice.getName(),is);	    	
		    imgDevice.showDisplay();
		    imgDevice.registerListener();
	    	synchronizer.addDevice(imgDevice);	    	
	    } else
	    if ((sample.getClass()==float.class)||(sample.getClass()==double.class)) {
	    	timeIt = new UniformTimeIterator(samplerRefDevice, initialFrame, endFrame, stepFrame);
		   	//System.out.println("You want to resample a numeric value");
	    	// Unsupported yet
		}
	    if ((sample.getClass()==float[].class)||(sample.getClass()==double[].class)) {
	    	timeIt = new UniformTimeIterator(samplerRefDevice, initialFrame, endFrame, stepFrame);
		   	//System.out.println("You want to resample a numeric array value");
	    	// Unsupported yet
		}				
	}
	
	
	
	

}
