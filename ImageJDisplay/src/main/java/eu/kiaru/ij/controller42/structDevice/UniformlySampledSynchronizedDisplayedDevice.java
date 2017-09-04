package eu.kiaru.ij.controller42.structDevice;

import java.time.Duration;
import java.time.LocalDateTime;

abstract public class UniformlySampledSynchronizedDisplayedDevice<T> extends DefaultSynchronizedDisplayedDevice implements Samplable<T> {	
	T currentSample;
	int currentSampleIndexDisplayed;
	private double avgTimeBetweenSamplesInMs;
	private int numberOfSamples;

	boolean samplingRateInitialized=false;
	
	/*public void setNumberOfSamples(int numberOfSamples) {
		this.numberOfSamples=numberOfSamples;
		avgTimeBetweenSamplesInMs = Duration.between(startAcquisitionTime,endAcquisitionTime).dividedBy(numberOfSamples-1).toNanos()/1e6;
		samplingRateInitialized=true;
	}*/	
	
	public int getNumberOfSamples() {
		if (samplingRateInitialized) {
			return numberOfSamples;
		} else {
			return -1;
		}
	}
	
	public synchronized int getCurrentSampleIndexDisplayed() {
		return currentSampleIndexDisplayed;
	}
	
	public void setSamplingInfos(LocalDateTime startAcqu, int nSamples, double avgTimeBetweenSamples) {
		this.startAcquisitionTime=startAcqu;
		this.numberOfSamples=nSamples;
		this.avgTimeBetweenSamplesInMs=avgTimeBetweenSamples;
		//endAcquisitionTime=this.startAcquisitionTime.plus(Duration.between(timeIni, timeEnd)).plusNanos((long)(avgTimeBetweenImagesInMs*1e6));
		Duration acquDuration = Duration.ofNanos((long)(avgTimeBetweenSamplesInMs*1e6)).multipliedBy(nSamples);
		this.endAcquisitionTime = this.startAcquisitionTime.plus(acquDuration);
		samplingRateInitialized=true;
	}
	
	public void setSamplingInfos(LocalDateTime startAcqu, LocalDateTime endAcqu, int nSamples) {
		this.startAcquisitionTime=startAcqu;
		this.endAcquisitionTime=endAcqu;
		numberOfSamples=nSamples;
		avgTimeBetweenSamplesInMs = Duration.between(startAcquisitionTime,endAcquisitionTime).dividedBy(numberOfSamples).toNanos()/1e6;
		samplingRateInitialized=true;		
	}
	
	public void copySamplingInfos(UniformlySampledSynchronizedDisplayedDevice device) {
		this.setSamplingInfos(device.startAcquisitionTime, device.endAcquisitionTime, device.getNumberOfSamples());
	}

	
	public void notifyNewSampleDisplayed(int newDisplayedSample) {
		if (newDisplayedSample!=this.currentSampleIndexDisplayed) {
			double durationInNS = avgTimeBetweenSamplesInMs*(newDisplayedSample-1)*1e6+avgTimeBetweenSamplesInMs/2.0;
			this.setCurrentTime(this.startAcquisitionTime.plusNanos((long)durationInNS).minus(this.getDisplayedTimeShift()));			
			this.currentSampleIndexDisplayed=newDisplayedSample;
			this.fireDeviceTimeChangedEvent();
		}
	}

	@Override
	final synchronized public void setDisplayedTime(LocalDateTime time) {
		if (this.samplingRateInitialized) {
		// Needs to find the correct image number
		Duration timeInterval = Duration.between(this.startAcquisitionTime,time);//.dividedBy(numberOfImages-1).toNanos()
		double timeIntervalInMs = (timeInterval.getSeconds()*1000+timeInterval.getNano()/1e6);
		int newSampleDisplayed = (int) (timeIntervalInMs/avgTimeBetweenSamplesInMs);
		//newSampleDisplayed+=1;// because of IJ1 notation style
		if (newSampleDisplayed<0) {
			newSampleDisplayed=0;
		}
		if (newSampleDisplayed>numberOfSamples) {
			newSampleDisplayed=numberOfSamples;
		}
		if (newSampleDisplayed!=currentSampleIndexDisplayed) {
			currentSampleIndexDisplayed=newSampleDisplayed;
			// needs to update the window, if any
			displayCurrentSample();
		}
		} else {
			System.out.println("Sampling rate of device "+this.getName()+" not initialized");
		}
	}
	
	abstract public void displayCurrentSample();
	
	abstract public T getSample(int n);

}