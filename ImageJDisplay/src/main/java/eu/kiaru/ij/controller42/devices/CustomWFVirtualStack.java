package eu.kiaru.ij.controller42.devices;

import ij.*;
import ij.plugin.PlugIn;
import ij.process.*;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.ArrayList;

public class CustomWFVirtualStack extends VirtualStack implements PlugIn {
	int WIDTH; //= 512;
	int HEIGHT; //= 512;
	//final int SIZE; = 100;*/

	
	String attachedRawDataPrefixFile;
	File currentOpenFile;
	private String currentRawDataFile;
	int currentFileIndex=-1;
	int numberOfImages;
	//byte[] backgroundImg;
	byte[] bgPixels;

	public void run(String arg) {
		ImageStack stack = new CustomWFVirtualStack(WIDTH, HEIGHT, 100, null, null);
		ImagePlus imp = new ImagePlus("Empty Virtual Stack", stack);
		imp.show();
	}

	public CustomWFVirtualStack() {
	}

	
	public CustomWFVirtualStack(int width, int height, int nSlices, ColorModel cm, String path) {
		super(width, height, cm, path);
		WIDTH=width;
		HEIGHT=height;
		setBitDepth(8);
		for (int i=0; i<nSlices; i++) {
				addSlice(""+(i+1));
		}
		bgPixels = new byte[WIDTH*HEIGHT];
		// Fetch BackgroungImage
		getBytesOfImg(0,bgPixels);
		
	}
	
	public void getBytesOfImg(int imgNumber, byte[] myBytes) {
		if (imgNumber==0) {
			for (int i=0;i<myBytes.length;i++) {
				myBytes[i]=(byte) i;
			}
		} else {
			for (int i=0;i<myBytes.length;i++) {
				myBytes[i]=(byte) (10*java.lang.Math.sin((60.0-imgNumber)/60.0*i/750));
			}
		}
	}
	
	public void setAttachedDataPath(String path) {
      	attachedRawDataPrefixFile=path;
      	initData();
	}
	
	ArrayList<Integer> indexOfStartingImageInFile;
	int numberOfFiles;
	public void initData() {
		int index=0;
		String suffix="_"+index+".raw";
		File tmpFile;
		int currentIndexImage=1;
		indexOfStartingImageInFile = new ArrayList<>();
		indexOfStartingImageInFile.add(currentIndexImage);
		do {
			tmpFile = new File(attachedRawDataPrefixFile+suffix);
			if (tmpFile.exists()) {
				int numberOfImage = (int)(tmpFile.length()/((long)WIDTH*(long)HEIGHT));
				currentIndexImage+=numberOfImage;
				indexOfStartingImageInFile.add(currentIndexImage);				
			}
			index++;
			suffix="_"+index+".raw";
		} while (tmpFile.exists());
		/*for (Integer ind:indexOfStartingImageInFile) {
			System.out.println("index="+ind);
		}*/
	}

	/*boolean isSliceInCurrentOpenedFile(int n) {
		return 
	}*/
	
	public ImageProcessor getProcessor(int n) {		
		ByteProcessor bp;
		byte[] rawData = new byte[WIDTH*HEIGHT];
		getBytesOfImg(n,rawData);
		
		bp = new ByteProcessor(WIDTH,HEIGHT,rawData);
		float offset = 0.5f;  //includes 0.5 for rounding when converting float to byte
		//See https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/BackgroundSubtracter.java		
		for (int p=0; p<bgPixels.length; p++) {
            float value = (rawData[p]&0xff) - bgPixels[p] + offset;
            if (value<0f) value = 0f;

            if (value>255f) value = 255f;

            rawData[p] = (byte)(value);
        }
		// raw is actually not raw
		return new ByteProcessor(WIDTH,HEIGHT,rawData);
	}

}
