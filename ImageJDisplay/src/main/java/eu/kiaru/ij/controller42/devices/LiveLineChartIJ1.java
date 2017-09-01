package eu.kiaru.ij.controller42.devices;
import ij.gui.Plot;

public class LiveLineChartIJ1 {
	public Plot plot;
	
	public LiveLineChartIJ1(java.lang.String title, java.lang.String xLabel, java.lang.String yLabel, float[] xValues, float[] yValues) {
		plot = new Plot(title, xLabel, yLabel, xValues, yValues);
		plot.show();
	}
}
