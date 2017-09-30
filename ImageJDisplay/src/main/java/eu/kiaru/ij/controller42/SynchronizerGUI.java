package eu.kiaru.ij.controller42;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.scijava.command.Command;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import eu.kiaru.ij.controller42.structDevice.DefaultSynchronizedDisplayedDevice;

@Plugin(type = Command.class, menuPath = "Controller 42>Synchronizer options")
public class SynchronizerGUI implements Command{
	
	@Parameter
	String synchronizerID;
	
    @Parameter
    private ObjectService objService;
	
	@Override
	public void run() {		
		DSDevicesSynchronizer mySync=null;
		for (DSDevicesSynchronizer synchronizer : objService.getObjects(DSDevicesSynchronizer.class)) {
			if (synchronizer.id.equals(synchronizerID)) {
				mySync=synchronizer;
				break;
			}			
		};
		if (mySync==null) {
			System.err.println("Synchronizer id not found!");
			return;
		}
		
		frame = new JFrame("Synchronizer options.");
		frame.setSize(400, 100);		
		idSynchronizerLabel = new JTextField("Synchronizer ID = "+idSynchronizer);
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
	public String idSynchronizer;

}
