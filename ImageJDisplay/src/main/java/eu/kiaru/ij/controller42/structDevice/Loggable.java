package eu.kiaru.ij.controller42.structDevice;

import java.io.File;

public interface Loggable {
	File getLogFile();
	void setLogFile(File f);
	int getLogVersion();
	void setLogVersion(int version);
}
