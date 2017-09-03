package eu.kiaru.ij.controller42.devices42;

import java.io.File;
import java.time.LocalDateTime;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;

abstract public class DefaultDevice42 extends DefaultSynchronizedDisplayedDevice {
	File logFile;
	int logFileVersion;

}
