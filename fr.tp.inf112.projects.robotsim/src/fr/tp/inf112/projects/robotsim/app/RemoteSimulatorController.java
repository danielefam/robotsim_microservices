package fr.tp.inf112.projects.robotsim.app;

import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import fr.tp.inf112.projects.canvas.controller.CanvasViewerController;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;

public class RemoteSimulatorController extends SimulatorController {
	
	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName()); 
    private final HttpClient client;
    private Integer port = 90;
	private final String BASE_URL = "http://localhost:"+port.toString()+"/";
	
	public RemoteSimulatorController(CanvasPersistenceManager persistenceManager) {
		super(persistenceManager);
		client = HttpClient.newHttpClient();
	}

	public RemoteSimulatorController(Factory factoryModel, CanvasPersistenceManager persistenceManager) {
		super(factoryModel, persistenceManager);
		client = HttpClient.newHttpClient();
	}
	@Override
	public void startAnimation() {
		HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(BASE_URL+"startAnimation/"+this.getCanvas().getId()))
					.GET()
					.build();
		
		try {
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());
			ObjectMapper objectMapper = new ObjectMapper();
			Boolean bool = objectMapper.readValue(response.body(), Boolean.class);
			
			if(bool == true) {
				super.startAnimation();
				// ricontrolla se giusto
				// IMPORTANTE
				updateViewer();
			}
			else {
				LOGGER.info("animation failed starting");
			}
		} catch (IOException | InterruptedException | URISyntaxException e) {
			// TODO Auto-generated catch block
			LOGGER.warning("startAnimation response failed");
		}
		
	}

	@Override
	public void stopAnimation() {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL+"stopAnimation/"+this.getCanvas().getId()))
				.GET()
				.build();
		
		try {
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());
			ObjectMapper objectMapper = new ObjectMapper();
			Boolean bool = objectMapper.readValue(response.body(), Boolean.class);
			
			if(bool == true)
				super.stopAnimation();
			else {
				LOGGER.info("animation failed stopping");
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.warning("startAnimation response failed");
		}
	}
	
	@Override
	public void setCanvas(final Canvas canvasModel) {
		if(!(getCanvas() instanceof Factory))
			return;
		
		final List<Observer> observers =((Factory) getCanvas()).getObservers();
		super.setCanvas(canvasModel);
		
		for (final Observer observer : observers) {
			((Factory) getCanvas()).addObserver(observer);
		}
		((Factory) getCanvas()).notifyObservers();
	}
	
	private void updateViewer() throws InterruptedException, URISyntaxException, IOException {
		if(!(getCanvas() instanceof Factory))
			return; 
		
		final URI uri = URI.create(BASE_URL+"retrieveFactory/"+this.getCanvas().getId());
		
		while (((Factory) getCanvas()).isSimulationStarted()) {
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.GET()
					.build();
			
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());	
			if(response == null)
				break;
			ObjectMapper objectMapper = new ObjectMapper();
			final Factory remoteFactoryModel = objectMapper.readValue(response.body(), Factory.class);
			
			setCanvas(remoteFactoryModel);
			Thread.sleep(100);
		}
	}
}
