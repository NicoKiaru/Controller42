package eu.kiaru.ij.controller42.structDevice;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

abstract public class DefaultSynchronizedDisplayedDevice<T> implements Synchronizable, Displayable, Samplable<T>{
	private LocalDateTime currentTime;
	boolean isDisplayed = false;
	boolean isSynchronized = true;
	private String name;
	private Set<DeviceListener> listeners = new HashSet<>();
	private Duration displayedTimeShift = Duration.ZERO;
	boolean firstShowDisplayCall=true;
	
	public LocalDateTime startAcquisitionTime,endAcquisitionTime;
	
	
	//public LocalDateTime instantAcquisitionStarted;
	//public Object deviceReferenceObject;	
	
	public DefaultSynchronizedDisplayedDevice() {
		//this.initDisplay();
	}
	
	// Ways this can be initialized
	abstract public void initDevice();
	abstract public void initDevice(File f, int version);
	
	public void setDisplayedTimeShift(Duration shift) {
		displayedTimeShift=shift;
		updateDisplay();
	}
	
	public Duration getDisplayedTimeShift() {
		return displayedTimeShift;
	}
	
	@Override
	public synchronized LocalDateTime getCurrentTime() {
		// TODO Auto-generated method stub
		return currentTime;
	}

	@Override
	public synchronized void setCurrentTime(LocalDateTime date) {
		currentTime=date;
		updateDisplay();		
	}
	
	private void updateDisplay() {
		if (isDisplayed) {			
			this.setDisplayedTime(currentTime.plus(displayedTimeShift));
		}
	}
	
	public final void showDisplay() {
		// TODO Auto-generated method stub
		if (this.firstShowDisplayCall) {
			this.initDisplay();
			firstShowDisplayCall=false;
		}
		isDisplayed = true;
		makeDisplayVisible();
	}
	
	public final void killDisplay() {
		isDisplayed=false;
		firstShowDisplayCall=true;
		closeDisplay();
	}
	
	abstract protected void makeDisplayVisible();
	abstract public void initDisplay();
	abstract public void closeDisplay();
	
	public boolean isDisplayed() {
		return isDisplayed;
	}
	
	public boolean isSynchronized() {
		return isSynchronized;
	}
	
	public void enableSynchronization() {
		isSynchronized=true;
	}
	
	public void disableSynchronization() {
		isSynchronized=false;
	}	
	
	@Override
	public final void hideDisplay() {
		// TODO Auto-generated method stub
		isDisplayed = false;
		makeDisplayInvisible();
	}
	
	abstract protected void makeDisplayInvisible();
	
	
	@Override
	abstract public void setDisplayedTime(LocalDateTime time);
	
	public synchronized void addDeviceListener(DeviceListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeDeviceListener(DeviceListener listener) {
		listeners.remove(listener);
	}
	
	protected synchronized void fireDeviceTimeChangedEvent() {
		DeviceEvent e = new DeviceEvent(this);
		for (DeviceListener listener : listeners) {
			listener.deviceTimeChanged(e);
		}
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getName() {
		return name;
	}
	

}
