package eu.kiaru.ij.controller42;

public class DeviceEvent {
	private DefaultSynchronizedDisplayedDevice source;
	
	public DeviceEvent(DefaultSynchronizedDisplayedDevice source) {
		this.source=source;
	}
	
	public DefaultSynchronizedDisplayedDevice getSource() {
		return source;
	}
}
