package eu.kiaru.ij.controller42;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;

@Plugin(type = Command.class, menuPath = "Controller 42>List devices")
public class SynchronizerGUI implements Command{
	
	@Parameter
	DSDevicesSynchronizer mySync;
	
    @Parameter
    private ObjectService objService;
	
	@Override
	public void run() {
		
		frame = new JFrame("Synchronizer options.");
		frame.setSize(400, 100);		
		idSynchronizerLabel = new JTextField("Synchronizer ID = "+mySync.id);
		idSynchronizerLabel.setEditable(false);
		JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(1+mySync.getDevices().size(),1));
		myPanel.add(idSynchronizerLabel);
		for (DefaultSynchronizedDisplayedDevice device:mySync.getDevices().values()) {	
			JTextField tf = new JTextField(device.getName());			
			tf.setEditable(false);
			myPanel.add(tf);
		}		
		frame.add(myPanel);		
		frame.setVisible(true);
	}
	
	JFrame frame;
	JTextField idSynchronizerLabel;

}
