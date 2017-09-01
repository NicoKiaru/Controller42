package eu.kiaru.ij.controller42;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DSDevicesSynchronizer implements DeviceListener{
	
	private Set<DefaultSynchronizedDisplayedDevice> devices;
	private Map<String,DefaultSynchronizedDisplayedDevice> devicesByName;
	
	public DSDevicesSynchronizer() {
		devices = new HashSet<>();
		devicesByName = new HashMap<>();
	}
	
	public Map<String,DefaultSynchronizedDisplayedDevice> getDevices() {
		return devicesByName;
	}
	
	public void addDevice(DefaultSynchronizedDisplayedDevice device) {
		devices.add(device);
		if (devicesByName.containsKey(device.getName())) {
			System.err.println("Error : devices with duplicate names!");
		}
		devicesByName.put(device.getName(), device);
		device.addDeviceListener(this);
	}
	
	public void removeDevice(DefaultSynchronizedDisplayedDevice device) {
		device.removeDeviceListener(this);
		devices.remove(device);
		devicesByName.remove(device.getName());
	}

	@Override
	public void deviceTimeChanged(DeviceEvent e) {		
		//System.out.println("Device "+e.getSource()+" has changed its time.");
		LocalDateTime broadcastedDate = e.getSource().getCurrentTime();		
		for (DefaultSynchronizedDisplayedDevice device : devices) {
			if ((device.isSynchronized)&&(!device.equals(e.getSource()))) {
				device.setCurrentTime(broadcastedDate);
			}
		}		
	}
	
}
