package eu.kiaru.ij.controller42.stdDevices;
import java.io.File;
import java.time.LocalDateTime;

import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import eu.kiaru.ij.slidebookExportedTiffOpener.CalibrationTimeOrigin;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class ImagePlusDeviceUniformlySampled extends UniformlySampledSynchronizedDisplayedDevice<ImageProcessor> implements ImageListener {

	public ImagePlus myImpPlus;
	
	@Override
	public void displayCurrentSample() {
		if (myImpPlus!=null) {
			if (!myImpPlus.isHyperStack()) {
				myImpPlus.setSlice((int) this.getCurrentSampleIndexDisplayed()+1);
			} else {
				int[] stackPos = myImpPlus.convertIndexToPosition(myImpPlus.getCurrentSlice());
				//System.out.println("frame="+((int) this.getCurrentSampleIndexDisplayed()+1));
				myImpPlus.setPosition(stackPos[0], stackPos[1], (int) this.getCurrentSampleIndexDisplayed()+1);
			}
		}
	}

	@Override
	public ImageProcessor getSample(int n) {
		if (myImpPlus!=null) {
			if (!myImpPlus.isHyperStack()) {
				return myImpPlus.getStack().getProcessor(n);
			} else {
				System.err.println("Unsupported getSample method in "+this.getClass().getName());
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void closeDisplay() {
		myImpPlus.close();
	}
	
	@Override
	public void imageUpdated(ImagePlus src) {
		if (src.getTitle().equals(this.getName())) {		
			if (!myImpPlus.isHyperStack()) {
				this.notifyNewSampleDisplayed(src.getCurrentSlice()-1); // because IJ1 notation
			} else {
				this.notifyNewSampleDisplayed(src.getFrame()-1);
			}
		}
	}
	
	@Override
	public void makeDisplayVisible() {
		System.out.println("Makes the imageplus "+this.getName()+" visible.");
		myImpPlus.show();
	}

	@Override
	public void makeDisplayInvisible() {
		myImpPlus.hide();
	}

	@Override
	public void imageClosed(ImagePlus arg0) {
	}

	@Override
	public void imageOpened(ImagePlus arg0) {
		
	}

	@Override
	public void initDevice() {
	}

	@Override
	public void initDevice(File f, int version) {
		// TODO Auto-generated method stub
		//ij.IJ.open(f.getAbsolutePath());
		initDevice(IJ.openImage(f.getAbsolutePath()));	
	}
	
	public void initDevice(ImagePlus myImgPlus) {
		this.myImpPlus=myImgPlus;
		System.out.println("myImpPlus is null="+(myImpPlus==null));
		this.setName(myImpPlus.getTitle());
		System.out.println("fps="+this.myImpPlus.getCalibration().fps);
		System.out.println("frameinterval="+this.myImpPlus.getCalibration().frameInterval);
		System.out.println("info="+this.myImpPlus.getCalibration().info);
		System.out.println("timeunit="+this.myImpPlus.getCalibration().getTimeUnit());
		System.out.println("calibrated()="+this.myImpPlus.getCalibration().calibrated());
		if (myImpPlus.getCalibration().getClass().equals(CalibrationTimeOrigin.class)) {
			//System.out.println("yes!");
			this.startAcquisitionTime=((CalibrationTimeOrigin)(myImpPlus.getCalibration())).startAcquisitionTime;
			this.setSamplingInfos(startAcquisitionTime, myImpPlus.getNFrames(), myImpPlus.getCalibration().frameInterval);
			/*System.out.println("startAcquisitionTime="+startAcquisitionTime);
			System.out.println("myImpPlus.getNFrames()="+myImpPlus.getNFrames());
			System.out.println("myImpPlus.getCalibration().frameInterval="+myImpPlus.getCalibration().frameInterval);*/
		} else {
			System.out.println("Warning : no extended calibration detected for the image.");
			System.err.println("Fetching acquisition time in metadata currently unsupported.");
		}
		myImpPlus.addImageListener(this);
	}

	@Override
	public void initDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ImageProcessor getSample(LocalDateTime date) {
		// TODO Auto-generated method stub
		return null;
	}
}
