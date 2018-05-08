package eu.kiaru.ij.controller42.structDevice;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

//Also linearly interpolated
abstract public class SparselySampledSynchronizedDisplayedDevice<T> extends DefaultSynchronizedDisplayedDevice<T> {	
	T currentSample;
	private int numberOfSamples;
	private boolean initialized;
	
	public ArrayList<TimedSample<T>> samples;

	/*public void setNumberOfSamples(int numberOfSamples) {
		this.numberOfSamples=numberOfSamples;
		avgTimeBetweenSamplesInMs = Duration.between(startAcquisitionTime,endAcquisitionTime).dividedBy(numberOfSamples-1).toNanos()/1e6;
		samplingRateInitialized=true;
	}*/	
	
	public int getNumberOfSamples() {
		if (initialized) {
			return numberOfSamples;
		} else {
			return -1;
		}		
	}
	
	public void samplesInitialized() {
		this.initialized=true;
	}

	@Override
	final synchronized public void setDisplayedTime(LocalDateTime time) {
		// Unsupported operation
		/*if (this.initialized) {
			// Needs to find the correct image number
			// Converted to local time since it doesn't work otherwise...
			Duration timeInterval = Duration.between(this.startAcquisitionTime.toLocalTime(),time.toLocalTime());//.dividedBy(numberOfImages-1).toNanos()
			double timeIntervalInMs = ((double)(timeInterval.getSeconds()*1000)+(double)((double)(timeInterval.getNano())/1e6));
			//newSampleDisplayed+=1;// because of IJ1 notation style
			boolean outOfBounds=false;
			/*if (newSampleDisplayed<0) {
				newSampleDisplayed=0;
				outOfBounds=true;
			} else
			if (newSampleDisplayed>numberOfSamples) {
				newSampleDisplayed=numberOfSamples;
				outOfBounds=true;
			} */
			
			// makeDisplayVisible / makeDisplayinvisible
			/*if (newSampleDisplayed!=currentSampleIndexDisplayed) {
				currentSampleIndexDisplayed=newSampleDisplayed;
				// needs to update the window, if any
				displayCurrentSample();
			}*/
			
		/*} else {
			System.out.println("Sampling of device "+this.getName()+" not initialized");
		}*/
	}
	
	
	public T getSample(LocalDateTime date) {
		// TODO Auto-generated method stub
		
		if (this.initialized) {
			// Needs to find the correct image number
			// Converted to local time since it doesn't work otherwise...
			
			
			LocalTime lt = date.toLocalTime();

			if (this.samples.isEmpty()) {
				return null;
			}
			
			int iSample = 0;

			while ((iSample<this.samples.size()) && (this.samples.get(iSample).time.isBefore(lt))) {
				iSample++;
			}

			
			if (iSample==0) {
				// Before any sample was acquired -> return sample 0
				return this.samples.get(0).sample;
			}
			
			if (iSample==this.samples.size()) {
				// After last sample was acquired
				return this.samples.get(this.samples.size()-1).sample;
			}
			
			// between iSample-1 and iSample
			
			Duration timeIntervalBefore = Duration.between(this.samples.get(iSample-1).time,date.toLocalTime());//.dividedBy(numberOfImages-1).toNanos()
			double timeIntervalInMsBefore = ((double)(timeIntervalBefore.getSeconds()*1000)+(double)((double)(timeIntervalBefore.getNano())/1e6));
			//System.out.println(timeIntervalInMsBefore);
			
			Duration timeIntervalTotal = Duration.between(this.samples.get(iSample-1).time,this.samples.get(iSample).time);//.dividedBy(numberOfImages-1).toNanos()
			double timeIntervalInMsTotal = ((double)(timeIntervalTotal.getSeconds()*1000)+(double)((double)(timeIntervalTotal.getNano())/1e6));
			//System.out.println(timeIntervalInMsTotal);
			
			return this.samples.get(0).interpolate(samples.get(iSample-1).sample, samples.get(iSample).sample, timeIntervalInMsBefore/timeIntervalInMsTotal);
			
			
			//long indexSample = java.lang.Math.round((double)(timeIntervalInMs/avgTimeBetweenSamplesInMs));

			/*if (indexSample<0) {
				indexSample=0;
			} else
			if (indexSample>=numberOfSamples) {
				indexSample=numberOfSamples-1;
			} */
			//return null;// TODO getSample((int)indexSample);
			
		} else {
			System.out.println("Sampling rate of device "+this.getName()+" not initialized");
			return null;
		}		

	}
	

}


