package eu.kiaru.ij.controller42.stdDevices;

import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;

public class LocalDateTimeDisplayer extends DefaultSynchronizedDisplayedDevice<LocalDateTime> {
	
	JFrame frame;
	JTextField idSynchronizerLabel;
	JLabel timeLabel;
	public String idSynchronizer;
	static String patternLocalDateTime = "yy-MM-dd HH:mm:ss.SSS";
	
	@Override
	public LocalDateTime getSample(LocalDateTime date) {
		return this.getCurrentTime();
	}

	@Override
	public void initDevice() {		
	}

	@Override
	public void initDevice(File f, int version) {
	}

	@Override
	protected void makeDisplayVisible() {
		frame.setVisible(true);		
	}

	String getStringOfCurrentLocalDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patternLocalDateTime);
		if (this.getCurrentTime()==null) return "undefined";
		return this.getCurrentTime().format(formatter);
	}
	
	@Override
	public void initDisplay() {
		frame = new JFrame(this.getName());
		frame.setSize(400, 100);
		timeLabel = new JLabel(patternLocalDateTime);
		idSynchronizerLabel = new JTextField("Synchronizer ID = "+idSynchronizer);
		idSynchronizerLabel.setEditable(false);
		JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(2,1));
		myPanel.add(idSynchronizerLabel);
		myPanel.add(timeLabel);
		frame.add(myPanel);
	}

	@Override
	public void closeDisplay() {
		frame.dispose();
	}

	@Override
	protected void makeDisplayInvisible() {
		frame.setVisible(false);		
	}

	@Override
	public void setDisplayedTime(LocalDateTime time) {
		timeLabel.setText(this.getStringOfCurrentLocalDateTime());		
	}

}
