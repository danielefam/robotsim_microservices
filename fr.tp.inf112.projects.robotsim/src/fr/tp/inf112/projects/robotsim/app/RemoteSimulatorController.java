package fr.tp.inf112.projects.robotsim.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactorySimulationEventConsumer;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.notifier.LocalNotifier;

public class RemoteSimulatorController extends SimulatorController {

	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());
    private final HttpClient client;
    private Integer port = 8080;
	private final String BASE_URL = "http://localhost:"+port.toString()+"/";
	private final ObjectMapper objectMapper;
	
	private final LocalNotifier localNotifier;

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
        localNotifier = new LocalNotifier();
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
        localNotifier = new LocalNotifier();
	}
	
	@Override
	public void startAnimation() {
		String id = this.getCanvas().getId();

		if(id == null) {
			this.getCanvas().setId("default.factory");
		} else {
			String[] aux = id.split("[/\\\\]");
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
			Boolean isStarted = objectMapper.readValue(response.body(), Boolean.class);
			
			if(isStarted) {
				((Factory)getCanvas()).setSimulationStarted(true);
				new Thread(() -> {
	                try {
	                	FactorySimulationEventConsumer consumer = new FactorySimulationEventConsumer(this);
	                    consumer.consumeMessages();
	                } catch (Exception e) {
	                    LOGGER.severe(e.getMessage());
	                }
	            }).start();
			}
			else {
				
//				if (uploadFactoryToServer((Factory) getCanvas())) {
//					LOGGER.info("chiamata");
//	                startAnimation();
//	            }
				
				getPersistenceManager().persist((Factory)getCanvas());
				startAnimation();
		        
			}
		}
		catch (MismatchedInputException e) {
			// TODO: handle exception
			LOGGER.warning("serialization error");
			LOGGER.severe(e.getMessage());
		}
		catch (IOException | InterruptedException e) {
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
			else{
				LOGGER.info("animation failed stopping");
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.warning("stopAnimation response failed");
		}
	} 
	
	public void setCanvas(String stringModel) throws JsonMappingException, JsonProcessingException {
		
		Factory readFactory = objectMapper.readValue(stringModel, Factory.class);
		LOGGER.fine(readFactory.toString());
		super.setCanvas(readFactory);
//		setCanvas(readFactory);
		LOGGER.fine("setcanvas, observers before: " + localNotifier.getObservers());
		localNotifier.notifyObservers();
		LOGGER.fine("setcanvas, observers after: " + localNotifier.getObservers());
	}
	
	
	private void releaseRemoteFactory() {
	    
	    HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(BASE_URL + "releaseFactory/" + this.getCanvas().getId()))
	            .GET()
	            .build();

	    try {
	        client.send(request, HttpResponse.BodyHandlers.ofString());
	    } catch (IOException | InterruptedException e) {
	        LOGGER.warning("Failed to release remote factory");
	    }
	}

	@Override
	public boolean addObserver(Observer observer) {
		LOGGER.finer("add observer");
	    return localNotifier.addObserver(observer); 
	}

	@Override
	public boolean removeObserver(Observer observer) {
		LOGGER.finer("remove observer");
	    boolean removed = localNotifier.removeObserver(observer);
	    
	    
	    if (localNotifier.getObservers().isEmpty()) {
	        LOGGER.finer("No more observers on the controller. Releasing remote factory...");
	        releaseRemoteFactory();
	    }
	    
	    return removed;
	}
	
}
