package eu.kiaru.ij.controller42;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

abstract public class DefaultSynchronizedDisplayedDevice implements SynchronizableDevice, DisplayableDevice{
	private Date currentTime;
	boolean isDisplayed = false;
	boolean isSynchronized = true;
	private String name;
	private Set<DeviceListener> listeners;
	
	
	public DefaultSynchronizedDisplayedDevice() {
		listeners = new HashSet<>();
		this.initDisplay();
	}
	
	@Override
	public synchronized Date getCurrentTime() {
		// TODO Auto-generated method stub
		return currentTime;
	}

	@Override
	public synchronized void setCurrentTime(Date date) {
		// TODO Auto-generated method stub
		currentTime=date;
		if (isDisplayed) {
			this.setDisplayedTime(currentTime);
		}
	}

	@Override
	public void initDisplay() {
		// TODO Auto-generated method stub
		isDisplayed = true;
		showDisplay();
	}	
	
	@Override
	public void closeDisplay() {
		// TODO Auto-generated method stub
		isDisplayed = false;
		removeDisplay();
	}
	
	abstract protected void removeDisplay();
	abstract protected void showDisplay();
	@Override
	abstract public void setDisplayedTime(Date time);
	
	public synchronized void addDeviceListener(DeviceListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeDeviceListener(DeviceListener listener) {
		listeners.remove(listener);
	}
	
	protected synchronized void deviceTimeChangedEvent() {
		DeviceEvent e = new DeviceEvent(this);
		for (DeviceListener listener : listeners) {
			listener.deviceTimeChanged(e);
		}
	}
	

}
