package eu.kiaru.ij.controller42;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DSDevicesSynchronizer implements DeviceListener{
	
	private Set<DefaultSynchronizedDisplayedDevice> devices;
	
	public DSDevicesSynchronizer() {
		devices = new HashSet<>();
	}
	
	public void addDevice(DefaultSynchronizedDisplayedDevice device) {
		devices.add(device);
		device.addDeviceListener(this);
	}
	
	public void removeDevice(DefaultSynchronizedDisplayedDevice device) {
		device.removeDeviceListener(this);
		devices.remove(device);
	}

	@Override
	public void deviceTimeChanged(DeviceEvent e) {		
		System.out.println("Device "+e.getSource()+" has changed its time.");
		Date broadcastedDate = e.getSource().getCurrentTime();		
		for (DefaultSynchronizedDisplayedDevice device : devices) {
			if ((device.isSynchronized)&&(!device.equals(e.getSource()))) {
				device.setCurrentTime(broadcastedDate);
			}
		}		
	}
	
}
