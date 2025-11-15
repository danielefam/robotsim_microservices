package fr.tp.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.RemoteFactoryPersistenceManager;

@RestController
public class RobotSimulationControllerApplcication {
	int port = 8081;
	Map<String, Factory> modelInSimulations = new HashMap<>();
	private static final Logger LOGGER = Logger.getLogger(RobotSimulationControllerApplcication.class.getName());
	final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
	private final RemoteFactoryPersistenceManager persistenceManager = new RemoteFactoryPersistenceManager(canvasChooser, port);
	
	@GetMapping("/startAnimation/{canvasId}")
	public boolean startAnimation(@PathVariable("canvasId") String canvasId) {
		Factory factory;
		LOGGER.info("start animation called");
		if(modelInSimulations.containsKey(canvasId)){
			LOGGER.info("Model already simulating");
			return false;
		}

		try {
			LOGGER.info("reading factory...");
			factory = (Factory) persistenceManager.read(canvasId);
			LOGGER.info("factory read");
			if(factory != null) {
				factory.startSimulation();
				modelInSimulations.put(canvasId, factory);
				LOGGER.info("Model id: " + canvasId + " started its simulation");
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.info("Model not found");
		return false;
		
	}
	
	@GetMapping("/stopAnimation/{canvasId}")
	public boolean stopAnimation(@PathVariable("canvasId") String canvasId) {
		if(modelInSimulations.containsKey(canvasId)){
			Factory factory = modelInSimulations.get(canvasId);
			factory.stopSimulation();
			modelInSimulations.remove(canvasId);
			LOGGER.info("Model id: " + canvasId + " stopped its simulation");
			return true;
		}
		LOGGER.info("Model id: " + canvasId + " was not running");
		return false;
	}
	
	@GetMapping("/retrieveFactory/{canvasId}")
	public Factory retrieveFactory(@PathVariable("canvasId") String canvasId){
		
		if(!modelInSimulations.containsKey(canvasId)){
			LOGGER.info("This model is not running");
			return null;
		}
		
		return modelInSimulations.get(canvasId);
	}
}
