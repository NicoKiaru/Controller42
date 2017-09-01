package eu.kiaru.ij.controller42;

import java.time.LocalDateTime;

public interface DisplayableDevice {
	void initDisplay();
	void closeDisplay();
	void setDisplayedTime(LocalDateTime time);
}
