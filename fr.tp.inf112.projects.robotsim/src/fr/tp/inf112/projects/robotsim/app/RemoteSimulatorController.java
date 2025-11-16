package fr.tp.inf112.projects.robotsim.app;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

public class RemoteSimulatorController extends SimulatorController {
	
	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName()); 
    private final HttpClient client;
    private Integer port = 8080;
	private final String BASE_URL = "http://localhost:"+port.toString()+"/";
	private final ObjectMapper objectMapper;
	
	public RemoteSimulatorController(CanvasPersistenceManager persistenceManager) {
		super(persistenceManager);
		client = HttpClient.newHttpClient();
		objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType(PositionedShape.class.getPackageName())
        .allowIfSubType(Component.class.getPackageName())
        .allowIfSubType(BasicVertex.class.getPackageName())
        .allowIfSubType(ArrayList.class.getName())
        .allowIfSubType(LinkedHashSet.class.getName())
        .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
	}

	public RemoteSimulatorController(Factory factoryModel, CanvasPersistenceManager persistenceManager) {
		super(factoryModel, persistenceManager);
		client = HttpClient.newHttpClient();
		objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType(PositionedShape.class.getPackageName())
        .allowIfSubType(Component.class.getPackageName())
        .allowIfSubType(BasicVertex.class.getPackageName())
        .allowIfSubType(ArrayList.class.getName())
        .allowIfSubType(LinkedHashSet.class.getName())
        .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
	}

	@Override
	public void startAnimation() {
		String id = this.getCanvas().getId();
		
		if(id == null)
			this.getCanvas().setId("default.factory");
		else {
			String[] aux = id.split("/");
			if(aux.length != 1){
				String filename;
				filename = aux[aux.length-1];
				this.getCanvas().setId(filename);
			}
		}
		
		HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(BASE_URL+"startAnimation/"+this.getCanvas().getId()))
					.GET()
					.build();
		
		try {
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());
//			ObjectMapper objectMapper = new ObjectMapper();
			LOGGER.info(response.body());
			Boolean bool = objectMapper.readValue(response.body(), Boolean.class);
			
			if(bool == true) {
				super.startAnimation();	
				LOGGER.fine("start: "+((Factory) getCanvas()).isSimulationStarted());
				updateViewer();
			}
			else {
				LOGGER.info("animation failed starting");
			}
		} 
		catch (MismatchedInputException e) {
			// TODO: handle exception
			LOGGER.warning("serialization error");
			LOGGER.severe(e.getMessage());
		}
		catch (IOException | InterruptedException | URISyntaxException e) {
			// TODO Auto-generated catch block
			LOGGER.warning("startAnimation response failed");
			LOGGER.severe(e.getMessage());
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
//			ObjectMapper objectMapper = new ObjectMapper();
			Boolean bool = objectMapper.readValue(response.body(), Boolean.class);
			
			if(bool == true)
				super.stopAnimation();
			else {
				LOGGER.info("animation failed stopping");
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.warning("stopAnimation response failed");
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
		Integer i = 0;
		while (((Factory) getCanvas()).isSimulationStarted()) {
			LOGGER.info("iter " + (i++).toString());
			HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.GET()
					.build();
			LOGGER.info("block 1");
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());	
			if(response == null)
				break;
			LOGGER.info("block 2");
			final Factory remoteFactoryModel = objectMapper.readValue(response.body(), Factory.class);
			setCanvas(remoteFactoryModel);
			Thread.sleep(100);
			LOGGER.info("block 3");
			LOGGER.info(""+((Factory) getCanvas()).isSimulationStarted());
		}
		LOGGER.info("block 4");
	}
}
