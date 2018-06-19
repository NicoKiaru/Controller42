/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package eu.kiaru.ij.controller42;

import org.scijava.command.Command;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Todo
 */
@Plugin(type = Command.class, menuPath = "Controller 42>Close Experiment")
public class CloseExperiment implements Command {
	//
    // Parameters here
    //
	@Parameter
	DSDevicesSynchronizer synchronizer;
	
	@Parameter
	ObjectService obj;

    @Override
    public void run() {    
    	synchronizer.getDevices().values().forEach(device -> {
    		device.hideDisplay();
    		device=null;
    	});
    	synchronizer.removeAllDevices();
    	obj.removeObject(synchronizer);
    	synchronizer=null;
    }
    

}
