package fr.tp.inf112.projects.robotsim.app;

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
//        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
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
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
	}

	@Override
	public void startAnimation() {
		String id = this.getCanvas().getId();

		if(id == null) {
			this.getCanvas().setId("default.factory");
		} else {
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

			if(bool) {
				// super.startAnimation();
				// LOGGER.fine("start: "+((Factory) getCanvas()).isSimulationStarted());
//				updateViewer();
//				if i do not start a new thread, i cannot stop the current one
				new Thread(() -> {
	                try {
	                    updateViewer();
	                } catch (Exception e) {
	                    LOGGER.severe(e.getMessage());
	                }
	            }).start();
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

	@Override
	public void setCanvas(final Canvas canvasModel) {
		
		if(!(getCanvas() instanceof Factory)) {
			return;
		}
//		Factory myFactory = (Factory) getCanvas();
//		final List<Observer> observers = myFactory.getObservers();
		final List<Observer> observers = new ArrayList<>(((Factory) getCanvas()).getObservers());
		super.setCanvas(canvasModel);

		for (final Observer observer : observers) {
			((Factory)canvasModel).addObserver(observer);
		}
		((Factory)canvasModel).notifyObservers();
	}

	private void updateViewer() throws InterruptedException, URISyntaxException, IOException {

		if(!(getCanvas() instanceof Factory)) {
			return;
		}
		final URI uri = URI.create(BASE_URL+"retrieveFactory/"+this.getCanvas().getId());
		LOGGER.info("uri: " + uri.toString());
		Integer i = 0;
		Factory remoteFactoryModel;
		do {
			LOGGER.fine("iter " + (i++).toString());
			HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.GET()
					.build();
			LOGGER.fine("block 1");
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString());
			if(response == null) {
				break;
			}
			LOGGER.fine("block 2");
			remoteFactoryModel = objectMapper.readValue(response.body(), Factory.class);
			setCanvas(remoteFactoryModel);
			Thread.sleep(100);
			LOGGER.fine("block 3");
			LOGGER.info("this: " +getCanvas().toString());
			LOGGER.info("remote: "+ remoteFactoryModel.toString());
			LOGGER.fine(""+((Factory) getCanvas()).isSimulationStarted());
		} while (remoteFactoryModel.isSimulationStarted());
//		stopAnimation();
		LOGGER.fine("block 4");
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
	public boolean removeObserver(Observer observer) {
		//I would like the simulation to remain in the memory of the simulated model on the spring boot 
		// controller when I pause it, but for it to be deleted when I close the window.
	    boolean removed = super.removeObserver(observer);
	    if (getCanvas() != null && ((Factory)getCanvas()).getObservers().isEmpty()) {
	        LOGGER.info("No more observers. Releasing remote factory...");
	        releaseRemoteFactory();
	    }
	    
	    return removed;
	}
	
}
