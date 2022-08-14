package holmes.obsolete;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;

public class Weird {
	public boolean forceSimulatorStop() {
		//final boolean success = true;
		GraphicalSimulator ns = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator();
		Timer t = ns.getTimer();
		if(t==null) {
			return true;
		}
		//ns.stop();
		//SimStop ss = new SimStop(ns);
		
        try {
        	ExecutorService pool = Executors.newFixedThreadPool(2);
        	pool.submit(new SimStop(ns)).get();
			boolean r = pool.submit(new SimWait(ns)).get();
			pool.shutdown();
			// Wait for everything to finish.
			while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
			  
			}
			return r;
			//pool.
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return false;
		
		//Process p1 = Runtime.getRuntime().exec(ns.stop());
		
		/*
		int safeCounter = 0;
		while(ns.getMode() != SimulatorMode.STOPPED) {
			try {
			    Thread.sleep(1000);
			    safeCounter++;
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			if(safeCounter == 5) {
				JOptionPane.showMessageDialog(null, "Error: simulator termination malfunction. Please stop&reset simulator manually.",
						"Simulator stopping failed.", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		if(ns.getMode() == SimulatorMode.STOPPED)
			return true;
		else
			return false;
*/
	}
	
	public static class SimStop implements Callable<Boolean> {
		GraphicalSimulator ns;
		public SimStop(GraphicalSimulator ns) {
			this.ns = ns;
		}
        public Boolean call() throws Exception {
            ns.stop();
            //Thread.sleep(2000);
            return true;
        }
    }
	
	public static class SimWait implements Callable<Boolean> {
		GraphicalSimulator ns;
		public SimWait(GraphicalSimulator ns) {
			this.ns = ns;
		}
        public Boolean call() throws Exception {
        	int safeCounter = 0;
    		while(ns.getSimulatorStatus() != SimulatorMode.STOPPED) {
    			try {
    			    Thread.sleep(1000);
    			    safeCounter++;
    			} catch(InterruptedException ex) {
    			    Thread.currentThread().interrupt();
    			}
    			
    			if(safeCounter == 5) {
    				//JOptionPane.showMessageDialog(null, "Error: simulator termination malfunction. Please stop&reset simulator manually.",
    				//		"Simulator stopping failed.", JOptionPane.ERROR_MESSAGE);
    				break;
    			}
    		}
    		
    		if(ns.getSimulatorStatus() == SimulatorMode.STOPPED)
    			return true;
    		else
    			return false;
        }
    }
}
