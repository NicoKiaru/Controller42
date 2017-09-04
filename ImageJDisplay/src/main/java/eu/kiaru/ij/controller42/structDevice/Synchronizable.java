package eu.kiaru.ij.controller42.structDevice;

import java.time.LocalDateTime;

public interface Synchronizable {
		LocalDateTime getCurrentTime();
		void setCurrentTime(LocalDateTime date);

}
