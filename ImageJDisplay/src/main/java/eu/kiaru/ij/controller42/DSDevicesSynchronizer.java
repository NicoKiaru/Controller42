package eu.kiaru.ij.controller42;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;
import eu.kiaru.ij.controller42.structDevice.DeviceEvent;
import eu.kiaru.ij.controller42.structDevice.DeviceListener;


public class DSDevicesSynchronizer implements DeviceListener{
	
	public String id;
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
		System.out.println(device.getName());
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
		//System.out.println("It is "+(broadcastedDate.toLocalTime()).toString()+" according to "+e.getSource().getName()+".");
		for (DefaultSynchronizedDisplayedDevice device : devices) {
			if ((device.isSynchronized())&&(!device.equals(e.getSource()))) {
				device.setCurrentTime(broadcastedDate);
			}
		}		
	}
	
}
