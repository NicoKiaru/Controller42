package eu.kiaru.ij.controller42.structDevice;

import java.time.LocalDateTime;

public interface Samplable<T> {
	public T getSample(LocalDateTime date);
}
