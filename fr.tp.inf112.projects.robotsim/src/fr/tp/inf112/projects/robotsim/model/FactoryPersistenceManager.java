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
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class FactoryPersistenceManager extends AbstractCanvasPersistenceManager {
	private static final Logger LOGGER = Logger.getLogger(FactoryPersistenceManager.class.getName());

	public FactoryPersistenceManager(final CanvasChooser canvasChooser) {
		super(canvasChooser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Canvas read(final String canvasId)
	throws IOException {
		String currentDir = System.getProperty("user.dir");
		LOGGER.info("path work: " + currentDir);
		LOGGER.info("path base: " + canvasId);
		// if run on windows split("\\\\")
		String[] aux = canvasId.split("/");
		String filename;
		filename = aux[aux.length-1];
		// if run on windows LOGGER.info("path read: " + currentDir+"\\canvas\\"+filename);
		LOGGER.info("path read: " + currentDir+"/canvas/"+filename);
		// the canvas is saved inside the directory canvas inside the project containing the model and the web server
		// it would have been better to use an absolute path, but for the project scope it is inconvenient
		try (
				// if on windows new FileInputStream(currentDir+"\\canvas\\"+filename);
			final InputStream fileInputStream = new FileInputStream(currentDir+"/canvas/"+filename);
			final InputStream bufInputStream = new BufferedInputStream(fileInputStream);
			final ObjectInputStream objectInputStrteam = new ObjectInputStream(bufInputStream);
		) {
			
			return (Canvas) objectInputStrteam.readObject();
		}
		catch (ClassNotFoundException | IOException ex) {
			throw new IOException(ex);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void persist(Canvas canvasModel)
	throws IOException {
		try (
			final OutputStream fileOutStream = new FileOutputStream(canvasModel.getId());
			final OutputStream bufOutStream = new BufferedOutputStream(fileOutStream);
			final ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream);
		) {	
			objOutStream.writeObject(canvasModel);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(final Canvas canvasModel)
	throws IOException {
		final File canvasFile = new File(canvasModel.getId());
		
		return canvasFile.delete();
	}
}
