package eu.kiaru.ij.controller42.devices42;

import ij.*;
import ij.plugin.PlugIn;
import ij.process.*;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CustomWFVirtualStack42 extends VirtualStack {//implements PlugIn {
	final int WIDTH; //= 512;
	final int HEIGHT; //= 512;
	//final int SIZE; = 100;*/

	
	String attachedRawDataPrefixFile;
	InputStream currentInputStream;
	RandomAccessFile currentInputFile; 
	FileChannel currentChannel;
	//private String currentRawDataFile;	
	
	int numberOfImages;
	//byte[] backgroundImg;
	byte[] bgPixels;
	
	//File currentOpenedFile;
	int currentFileIndex=-1;
	

	/*public void run(String arg) {
		ImageStack stack = new CustomWFVirtualStack42(WIDTH, HEIGHT, 100, null, null);
		ImagePlus imp = new ImagePlus("Empty Virtual Stack", stack);
		imp.show();
	}*/
	
	public CustomWFVirtualStack42(int width, int height, int nSlices, ColorModel cm, String path) {
		super(width, height, cm, path);
		WIDTH=width;
		HEIGHT=height;
		setBitDepth(8);
		for (int i=0; i<nSlices; i++) {
				addSlice(""+(i+1));
		}	
	}
	
	public void getBytesOfImg(int imgNumber, byte[] myBytes) {
		int minIndex;
		int maxIndex;
		if (currentFileIndex==-1) {
			currentFileIndex=0;
			minIndex = indexOfStartingImageInFile.get(0);
			maxIndex = indexOfStartingImageInFile.get(1);
			String fileName = attachedRawDataPrefixFile+"_0.raw";
			// Needs to close and open need data file
			try {
				currentInputFile = new RandomAccessFile(fileName, "r");
				currentChannel = currentInputFile.getChannel();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("Couldn't find file "+fileName);
			}
		} else {
			minIndex = indexOfStartingImageInFile.get(currentFileIndex);
			maxIndex = indexOfStartingImageInFile.get(currentFileIndex+1);
		}

		if (!((imgNumber>=minIndex)&&(imgNumber<maxIndex))) {
			try {
				currentChannel.close();
				currentInputFile.close();
			} catch (IOException e1) {
				// Necessary to be there on first call -> we don't care
			}
			currentFileIndex=0;
			while (imgNumber>=indexOfStartingImageInFile.get(currentFileIndex+1)) {
				currentFileIndex++;
			}
			
			String fileName = attachedRawDataPrefixFile+"_"+currentFileIndex+".raw";
			try {
				currentInputFile = new RandomAccessFile(fileName, "r");
				currentChannel = currentInputFile.getChannel();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("Couldn't find file "+fileName);
			}			
		}
		try {
			long position = ((long)(imgNumber-indexOfStartingImageInFile.get(currentFileIndex)))*((long) WIDTH*HEIGHT);
			ByteBuffer copy = ByteBuffer.allocate(WIDTH*HEIGHT*2);

			currentChannel.position(position);
		    currentChannel.read(copy);
			copy.flip();
			copy.get(myBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeFiles() {
		System.out.println("Closing has been called!!");
		try {
			currentChannel.close();
			currentInputFile.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	float avgBG;
	
	public void setAttachedDataPath(String path) {
      	attachedRawDataPrefixFile=path;
      	initData();
      	// also initializes the background
		bgPixels = new byte[WIDTH*HEIGHT];
		getBytesOfImg(0,bgPixels);
		long pixSum=0;
		for (int i=0;i<WIDTH*HEIGHT;i++) {
			pixSum+=(bgPixels[i]&0xff);
		}
		avgBG = (float)((double)pixSum/(double)(WIDTH*HEIGHT));
	}
	
	ArrayList<Integer> indexOfStartingImageInFile;
	int numberOfFiles;
	public void initData() {
		int index=0;
		String suffix="_"+index+".raw";
		File tmpFile;
		int currentIndexImage=0;
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
	}
	
	/**
	 * synchronized to avoid concurrent access : the channel is shared for display and processing
	 */
	public synchronized ImageProcessor getProcessor(int n) {		// 
		byte[] rawData = new byte[WIDTH*HEIGHT];
		getBytesOfImg(n,rawData);
		//See https://github.com/imagej/imagej1/blob/master/ij/plugin/filter/BackgroundSubtracter.java		
		for (int p=0; p<bgPixels.length; p++) {
			 //float value = (rawData[p]&0xff) - (bgPixels[p]&0xff) + offset;
			float value = (rawData[p]&0xff) - (bgPixels[p]&0xff) + avgBG;
            if (value<0f) value = 0f;

            if (value>255f) value = 255f;

            rawData[p] = (byte)(value);
        }
		byte[] transposedData = new byte[WIDTH*HEIGHT];
		for (int x=0;x<WIDTH;x++) {
			for (int y=0;y<HEIGHT;y++) {
				transposedData[x+y*WIDTH]=rawData[y+x*HEIGHT];
			}
		}
		// raw is actually not raw
		return new ByteProcessor(WIDTH,HEIGHT,transposedData);
	}

}
