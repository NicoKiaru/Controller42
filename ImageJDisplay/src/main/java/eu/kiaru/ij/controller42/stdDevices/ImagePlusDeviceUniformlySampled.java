package eu.kiaru.ij.controller42.stdDevices;
import java.io.File;
import java.time.LocalDateTime;

import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;
import eu.kiaru.ij.slidebookExportedTiffOpener.CalibrationTimeOrigin;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import loci.common.services.ServiceFactory;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.BF;

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
		} else {
			System.out.println("Warning : no extended calibration detected for the image.");
			System.err.println("Fetching acquisition time in metadata currently unsupported.");
			System.out.println("myImpPlus.getStartTime()="+myImpPlus.getStartTime());
	/*		 ServiceFactory factory = new ServiceFactory();
			    OMEXMLService service = factory.getInstance(OMEXMLService.class);
			    IMetadata meta = service.createOMEXMLMetadata();
			    // or if you want a specific schema version, you can use:
			    //IMetadata meta = service.createOMEXMLMetadata(null, "2009-02");
			    //meta.createRoot();

			    // create format reader
			    IFormatReader reader = new ImageReader();
			    reader.setMetadataStore(meta);

			    // initialize file
			    System.out.println("Fetching metadata of  " + this.getName());
			    reader.setId(this.getName());
			    

			    int seriesCount = reader.getSeriesCount();
			    if (series < seriesCount) reader.setSeries(series);
			    series = reader.getSeries();
			    System.out.println("\tImage series = " + series + " of " + seriesCount);

			    printDimensions(reader);
			    printGlobalTiming(meta, series);
			    printTimingPerTimepoint(meta, series);
			    printTimingPerPlane(meta, series);*/
		}
		this.registerListener();
	}
	
	public void registerListener() {
		myImpPlus.addImageListener(this);
	}

	@Override
	public void initDisplay() {
		// TODO Auto-generated method stub
		
	}
}
