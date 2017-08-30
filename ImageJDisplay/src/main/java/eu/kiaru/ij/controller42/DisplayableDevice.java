package eu.kiaru.ij.controller42;

import java.util.Date;

public interface DisplayableDevice {
	void initDisplay();
	void closeDisplay();
	void setDisplayedTime(Date time);
}
