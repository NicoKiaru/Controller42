package eu.kiaru.ij.slidebookExportedTiffOpener;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.VirtualStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class ExportedSBVirtualStack extends VirtualStack {
	
	final String[][] FILENAMES;
	final int WIDTH, HEIGHT, NFRAMES, NCHANNELS, NZSLICES;
	final ImagePlus[][] backingImPs;
	final boolean hasZSlices;
	
	ImagePlus linkedHyperStack;
	
	static public int[] getDimensions(String probedFile) {
		ImagePlus imp =  IJ.openVirtual(probedFile);
		int[] dims = new int[] {imp.getWidth(),imp.getHeight(),imp.getNSlices()};
		imp.close();
		return dims;
	}
	
	public ExportedSBVirtualStack(String folderPath, String[][] filenames, int width,int height, int nframes, int nchannels, boolean virtual) {
		super(width, height, null, null);
		hasZSlices=false;
		FILENAMES=filenames;
		WIDTH=width;
		HEIGHT=height;
		NFRAMES=nframes;
		NCHANNELS=nchannels;
		NZSLICES=1;

		backingImPs = new ImagePlus[NFRAMES][NCHANNELS];
		
		for (int channel=0;channel<NCHANNELS;channel++) {
			System.out.println("f=0; ch="+channel);
			if (virtual) {
				System.out.println(folderPath + File.separator + filenames[0][channel]);
				backingImPs[0][channel] =  IJ.openVirtual(folderPath + File.separator + filenames[0][channel]);
			} else {
				backingImPs[0][channel] =  IJ.openImage(folderPath + File.separator + filenames[0][channel]);
			}
		}
		
		for (int i=0; i<(NFRAMES*NCHANNELS*NZSLICES); i++) {
			addSlice(""+(i+1));
		}
	}
	
	public void setHyperStack(ImagePlus imgP) {
		this.linkedHyperStack=imgP;
	}

	public synchronized ImageProcessor getProcessor(int n) {
		if ((linkedHyperStack!=null)&&(!this.hasZSlices)) {
			int[] stackPos = linkedHyperStack.convertIndexToPosition(n);			
			return (this.backingImPs[0][stackPos[0]-1].getImageStack().getProcessor(stackPos[2]));
		} else {	
			return this.backingImPs[0][0].getImageStack().getProcessor(n);
		}
	}

}
