package eu.kiaru.ij.controller42.structTime;

import java.time.LocalDateTime;
import java.util.Iterator;

import eu.kiaru.ij.controller42.structDevice.UniformlySampledSynchronizedDisplayedDevice;

public class UniformTimeIterator implements Iterator<LocalDateTime>{
	
	private double avgTimeBetweenSamplesInMs;
	private int numberOfSteps;
	
	private int currentStep;
	
	public LocalDateTime startTime,endTime, currentTime;
	
	public UniformTimeIterator(UniformlySampledSynchronizedDisplayedDevice device) {
		this.startTime = device.startAcquisitionTime;
		this.endTime = device.endAcquisitionTime;
		this.numberOfSteps = device.getNumberOfSamples();
		this.avgTimeBetweenSamplesInMs = device.getMsBetweenSamples();		
	}
	
	public UniformTimeIterator(UniformlySampledSynchronizedDisplayedDevice device, int iStart, int iStop) {
		this.startTime = device.getLocalTimeAt(iStart);
		this.endTime = device.getLocalTimeAt(iStop);
		this.numberOfSteps = iStop-iStart;
		this.avgTimeBetweenSamplesInMs = device.getMsBetweenSamples();	
	}	
	
	public UniformTimeIterator(UniformlySampledSynchronizedDisplayedDevice device, int iStart, int iStop, int nSkippedSamples) {
		this.startTime = device.getLocalTimeAt(iStart);
		this.endTime = device.getLocalTimeAt(iStop);
		this.numberOfSteps = (int) ((iStop-iStart)/(nSkippedSamples));
		this.avgTimeBetweenSamplesInMs = device.getMsBetweenSamples()*nSkippedSamples;	
	}	
	
	public int getNumberOfSteps() {
		return numberOfSteps;
	}
	
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return currentStep<numberOfSteps;
	}
	
	@Override
	public LocalDateTime next() {
		// TODO Auto-generated method stub
		LocalDateTime ans=null;
		double durationInNS = avgTimeBetweenSamplesInMs*(currentStep)*1e6;
		ans = this.startTime.plusNanos((long)durationInNS);
		currentStep++;
		return ans;
	}
	
}
