package eu.kiaru.ij.controller42;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

abstract public class DefaultSynchronizedDisplayedDevice implements SynchronizableDevice, DisplayableDevice{
	private LocalDateTime currentTime;
	public boolean isDisplayed = false;
	boolean isSynchronized = true;
	private String name;
	private Set<DeviceListener> listeners;
	
	
	public DefaultSynchronizedDisplayedDevice() {
		listeners = new HashSet<>();
		//this.initDisplay();
	}
	
	@Override
	public synchronized LocalDateTime getCurrentTime() {
		// TODO Auto-generated method stub
		return currentTime;
	}

	@Override
	public synchronized void setCurrentTime(LocalDateTime date) {
		// TODO Auto-generated method stub
		currentTime=date;
		if (isDisplayed) {
			this.setDisplayedTime(currentTime);
		}
	}

	/*@Override
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
	}*/
	
	abstract protected void removeDisplay();
	abstract protected void showDisplay();
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
