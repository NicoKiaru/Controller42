package eu.kiaru.ij.controller42.structDevice;

import java.awt.Color;

import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.Roi;

public class MyPlot {
	public Plot plot;
	Roi lineCurrentTime;
	
	double pX=50;
	double h=50;
	
	public MyPlot() {
		
	}
	
	public void checkLineAtCurrentLocation(double n) {
		if (lineCurrentTime==null) {			
			ij.gui.Roi.setColor(new Color(150,50,50));
			ij.gui.Line.setWidth(2);
			ij.gui.Line.setColor(new Color(150,50,50));
			lineCurrentTime = new Line(pX,0,pX,h);
			if (plot.getImagePlus()!=null)
				if (plot.getImagePlus().getOverlay()!=null)
					plot.getImagePlus().getOverlay().add(lineCurrentTime);			
		}
		
		int testX = (int) plot.scaleXtoPxl((double) n);
		int testH = plot.getImagePlus().getHeight();
		
		if (testX!=pX) {
			pX=testX;
			if (plot.getImagePlus()!=null)
				if (plot.getImagePlus().getOverlay()!=null)
					plot.getImagePlus().getOverlay().remove(lineCurrentTime);
			ij.gui.Roi.setColor(new Color(150,50,50));
			ij.gui.Line.setWidth(2);
			ij.gui.Line.setColor(new Color(150,50,50));
			lineCurrentTime = new Line(pX,0,pX,h);
			if (plot.getImagePlus()!=null)
				if (plot.getImagePlus().getOverlay()!=null)
					plot.getImagePlus().getOverlay().add(lineCurrentTime);		
		}
		
		if (testH!=h) {
			h=testH;
			if (plot.getImagePlus()!=null)
				if (plot.getImagePlus().getOverlay()!=null)
					plot.getImagePlus().getOverlay().remove(lineCurrentTime);
			ij.gui.Roi.setColor(new Color(150,50,50));
			ij.gui.Line.setWidth(2);
			ij.gui.Line.setColor(new Color(150,50,50));lineCurrentTime = new Line(pX,0,pX,h);
			if (plot.getImagePlus()!=null)
				if (plot.getImagePlus().getOverlay()!=null)
					plot.getImagePlus().getOverlay().add(lineCurrentTime);	
		}
	}
	
}
