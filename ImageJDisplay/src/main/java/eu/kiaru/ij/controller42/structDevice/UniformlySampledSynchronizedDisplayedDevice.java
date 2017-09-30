package eu.kiaru.ij.controller42.structDevice;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

abstract public class UniformlySampledSynchronizedDisplayedDevice<T> extends DefaultSynchronizedDisplayedDevice<T> {	
	T currentSample;
	long currentSampleIndexDisplayed;
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
	
	public synchronized long getCurrentSampleIndexDisplayed() {
		return currentSampleIndexDisplayed;
	}
	
	public double getMsBetweenSamples() {
		return avgTimeBetweenSamplesInMs;
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
	
	public LocalDateTime getLocalTimeAt(int indexSample) {
		double durationInNS = avgTimeBetweenSamplesInMs*(indexSample)*1e6;
		if (indexSample>=0) {
			return this.startAcquisitionTime.plusNanos((long)durationInNS);
		} else {
			return this.startAcquisitionTime.minusNanos((long)(-durationInNS));
		}
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
			double durationInNS = avgTimeBetweenSamplesInMs*(newDisplayedSample)*1e6;//+avgTimeBetweenSamplesInMs/2.0;
			this.setCurrentTime(this.startAcquisitionTime.plusNanos((long)durationInNS).minus(this.getDisplayedTimeShift()));			
			this.currentSampleIndexDisplayed=newDisplayedSample;
			this.fireDeviceTimeChangedEvent();
		}
	}

	@Override
	final synchronized public void setDisplayedTime(LocalDateTime time) {
		if (this.samplingRateInitialized) {
			// Needs to find the correct image number
			// Converted to local time since it doesn't work otherwise...
			Duration timeInterval = Duration.between(this.startAcquisitionTime.toLocalTime(),time.toLocalTime());//.dividedBy(numberOfImages-1).toNanos()
			double timeIntervalInMs = ((double)(timeInterval.getSeconds()*1000)+(double)((double)(timeInterval.getNano())/1e6));
			long newSampleDisplayed = java.lang.Math.round((double)(timeIntervalInMs/avgTimeBetweenSamplesInMs));
			//newSampleDisplayed+=1;// because of IJ1 notation style
			boolean outOfBounds=false;
			if (newSampleDisplayed<0) {
				newSampleDisplayed=0;
				outOfBounds=true;
			} else
			if (newSampleDisplayed>numberOfSamples) {
				newSampleDisplayed=numberOfSamples;
				outOfBounds=true;
			} 
			
			// makeDisplayVisible / makeDisplayinvisible
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
	
	
	public T getSample(LocalDateTime date) {
		// TODO Auto-generated method stub
		
		if (this.samplingRateInitialized) {
			// Needs to find the correct image number
			// Converted to local time since it doesn't work otherwise...
			Duration timeInterval = Duration.between(this.startAcquisitionTime.toLocalTime(),date.toLocalTime());//.dividedBy(numberOfImages-1).toNanos()
			double timeIntervalInMs = ((double)(timeInterval.getSeconds()*1000)+(double)((double)(timeInterval.getNano())/1e6));
			long indexSample = java.lang.Math.round((double)(timeIntervalInMs/avgTimeBetweenSamplesInMs));

			if (indexSample<0) {
				indexSample=0;
			} else
			if (indexSample>=numberOfSamples) {
				indexSample=numberOfSamples-1;
			} 
			return getSample((int)indexSample);
			
		} else {
			System.out.println("Sampling rate of device "+this.getName()+" not initialized");
			return null;
		}		

	}
	

}
