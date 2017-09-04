package eu.kiaru.ij.controller42.stdDevices;
import java.io.File;

import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class ImagePlusDeviceUniformlySampled extends UniformlySampledSynchronizedDisplayedDevice<ImageProcessor> implements ImageListener {

	public ImagePlus myImpPlus;

	/*
	 * if (lms.workingImP!=null) {
            CurrSlice=lms.workingImP.getCurrentSlice();
            NSlices=lms.workingImP.getNSlices();
            NChannel=lms.workingImP.getNChannels();
            frameHasChanged = (CurrFrame!=((int)(CurrSlice-1)/(int)(NSlices*NChannel))+1);
            CurrFrame=((int)(CurrSlice-1)/(int)(NSlices*NChannel))+1;
            CurrZSlice=lms.workingImP.getSlice();
        } else {
            CurrSlice=1;NSlices=1;NChannel=1;CurrFrame=1;CurrZSlice=1;
            frameHasChanged=false;
        }(non-Javadoc)
	 * @see eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice#displayCurrentSample()
	 */
	
	@Override
	public void displayCurrentSample() {
		if (myImpPlus!=null) {
			//int CurrSlice=myImpPlus.getCurrentSlice();
            //int NSlices=myImpPlus.getNSlices();
            //int NChannel=myImpPlus.getNChannels();
            //frameHasChanged = (CurrFrame!=((int)(CurrSlice-1)/(int)(NSlices*NChannel))+1);
            int CurrFrame=(int)this.getCurrentSampleIndexDisplayed()+1;//((int)(CurrSlice-1)/(int)(NSlices*NChannel))+1;
            int CurrZSlice=myImpPlus.getSlice();
            int CurrChannel=1;
            myImpPlus.setPosition(CurrChannel, CurrZSlice, CurrFrame);
            //myImpPlus.setPositionWithoutUpdate(channel, slice, frame);
			//myImpPlus.setPosition((int)(this.getCurrentSampleIndexDisplayed())+1); // +1 ? because IJ1 notation
		}
	}

	@Override
	public ImageProcessor getSample(int n) {
		return null;//myImpPlus.getStack().getProcessor(n); (unsupported)
	}

	@Override
	public void closeDisplay() {
		myImpPlus.close();
	}
	
	@Override
	public void imageUpdated(ImagePlus src) {
		if (src.getTitle().equals(this.getName())) {			
			this.notifyNewSampleDisplayed(src.getCurrentSlice()-1); // because IJ1 notation
		}
	}
	
	@Override
	public void makeDisplayVisible() {
		myImpPlus.show();
	}

	@Override
	public void makeDisplayInvisible() {
		myImpPlus.hide();
	}

	@Override
	public void imageClosed(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageOpened(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initDevice() {
		// TODO Auto-generated method stub
		
		//System.out.println("fps="+this.myImpPlus.getCalibration().);
	}

	@Override
	public void initDevice(File f, int version) {
		// TODO Auto-generated method stub
		//ij.IJ.open(f.getAbsolutePath());
		myImpPlus = IJ.openImage(f.getAbsolutePath());	
		this.setName(myImpPlus.getTitle());
		System.out.println("fps="+this.myImpPlus.getCalibration().fps);
		System.out.println("frameinterval="+this.myImpPlus.getCalibration().frameInterval);
		System.out.println("info="+this.myImpPlus.getCalibration().info);
		System.out.println("timeunit="+this.myImpPlus.getCalibration().getTimeUnit());
		System.out.println("calibrated()="+this.myImpPlus.getCalibration().calibrated());
		
	
	}

	@Override
	public void initDisplay() {
		// TODO Auto-generated method stub
		
	}
}
