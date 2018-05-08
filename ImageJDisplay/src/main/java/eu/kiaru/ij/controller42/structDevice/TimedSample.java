package eu.kiaru.ij.controller42.structDevice;

import java.time.LocalTime;

abstract public class TimedSample<T> {
	public T sample;
	public LocalTime time;	
	abstract public T interpolate(T ti, T tf, double ratio_I_to_F);
}
