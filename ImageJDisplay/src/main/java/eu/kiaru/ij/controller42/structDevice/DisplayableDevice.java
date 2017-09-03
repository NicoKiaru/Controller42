package eu.kiaru.ij.controller42.structDevice;

import java.time.LocalDateTime;

public interface DisplayableDevice {
	void initDisplay();
	void showDisplay();
	void hideDisplay();
	void killDisplay();
	void setDisplayedTime(LocalDateTime time);
}
