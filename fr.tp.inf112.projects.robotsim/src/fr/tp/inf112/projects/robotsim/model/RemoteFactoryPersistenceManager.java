package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {	
	
	Socket socket;	
	int port;

	public RemoteFactoryPersistenceManager(){
		super(null);
		port = 8081;
	}
	
	public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser, int port) {
		super(canvasChooser);
		this.port = port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Canvas read(final String canvasId) throws IOException {
		try (
				Socket socket = new Socket("localhost", port);
				OutputStream outStr = socket.getOutputStream();
	        	ObjectOutputStream outObjectStream = new ObjectOutputStream(outStr);
				
				InputStream inpStr = socket.getInputStream();
				ObjectInputStream inpObjectStream = new ObjectInputStream(inpStr);
		){
			outObjectStream.writeObject(canvasId);
			return (Canvas) inpObjectStream.readObject();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void persist(Canvas canvasModel) throws IOException {
		
		try (
				Socket socket = new Socket("localhost", port);
				OutputStream outStr = socket.getOutputStream();
	        	ObjectOutputStream outObjectStream = new ObjectOutputStream(outStr);
		){
			outObjectStream.writeObject(canvasModel);
		} 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(final Canvas canvasModel) throws IOException {
		final File canvasFile = new File(canvasModel.getId());
		
		return canvasFile.delete();
	}
}

