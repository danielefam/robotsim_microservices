package fr.tp.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.RemoteFactoryPersistenceManager;

@RestController
public class RobotSimulationControllerApplication {
	int port = 8081;
	Map<String, Factory> modelInSimulations = new HashMap<>();
	private static final Logger LOGGER = Logger.getLogger(RobotSimulationControllerApplication.class.getName());
	final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
	private final RemoteFactoryPersistenceManager persistenceManager = new RemoteFactoryPersistenceManager(canvasChooser, port);
	
	@GetMapping("/startAnimation/{canvasId}")
	public boolean startAnimation(@PathVariable("canvasId") String canvasId) {
		Boolean isStarted = false;
		Factory factory;
		LOGGER.info("start animation called");
		if(modelInSimulations.containsKey(canvasId)){
			LOGGER.info("Model already simulating");
			if(!modelInSimulations.get(canvasId).isSimulationStarted()) {
				modelInSimulations.get(canvasId).startSimulation();
				LOGGER.info("Model id: " + canvasId + " started its simulation");
				isStarted = true;
				return isStarted;
			}
			return isStarted;
		}

		try {
			LOGGER.info("reading factory...");
			factory = (Factory) persistenceManager.read(canvasId);
			LOGGER.info("factory read");
			if(factory != null) {
				factory.setId(canvasId);
				factory.startSimulation();
				modelInSimulations.put(canvasId, factory);
				LOGGER.info("Model id: " + canvasId + " started its simulation");
				isStarted = true;
				return isStarted;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.info("Model not found");
		return isStarted;
		
	}
	
	@GetMapping("/stopAnimation/{canvasId}")
	public boolean stopAnimation(@PathVariable("canvasId") String canvasId) {
		Boolean isStopped = false;
		if(modelInSimulations.containsKey(canvasId)){
			Factory factory = modelInSimulations.get(canvasId);
			factory.stopSimulation();
//			modelInSimulations.remove(canvasId);
			LOGGER.info("Model id: " + canvasId + " stopped its simulation");
			isStopped = true;
			return isStopped;
		}
		LOGGER.info("Model id: " + canvasId + " was not running");
		return isStopped;
	}
	
	@GetMapping("/retrieveFactory/{canvasId}")
	public Factory retrieveFactory(@PathVariable("canvasId") String canvasId){
		LOGGER.info("id: "+canvasId);
		LOGGER.info("simulating: "+modelInSimulations.toString());
		if(!modelInSimulations.containsKey(canvasId)){
			LOGGER.info("This model is not running");
			return null;
		}
		return modelInSimulations.get(canvasId);
	}
	
	@GetMapping("/releaseFactory/{canvasId}")
    public boolean releaseFactory(@PathVariable("canvasId") String canvasId) {
        if (modelInSimulations.containsKey(canvasId)) {
            modelInSimulations.get(canvasId).stopSimulation();
            modelInSimulations.remove(canvasId);
            LOGGER.info("Model id: " + canvasId + " removed from memory (Client disconnected).");
            return true;
        }
        return false;
    }
}
