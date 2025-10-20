package fr.tp.inf112.projects.robotsim.model.serverutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

public class RequestProcessor implements Runnable {
    private Socket socket;
    private FactoryPersistenceManager localFileManager;

    public RequestProcessor(Socket socket) {
        this.socket = socket;
        localFileManager = new FactoryPersistenceManager(null);
    }

    @Override
    public void run() {
        try(
            Socket socket = this.socket;
            InputStream inpStr = socket.getInputStream();
        	ObjectInputStream inpObjectStream = new ObjectInputStream(inpStr);

            OutputStream outStr = socket.getOutputStream();
        	ObjectOutputStream outObjectStream = new ObjectOutputStream(outStr);
        ){
            Object receivedObject = inpObjectStream.readObject();
            if (receivedObject instanceof Factory) {
            	Factory factoryToSaveFactory = (Factory) receivedObject;
            	localFileManager.persist(factoryToSaveFactory);

            } else if (receivedObject instanceof String) {
            	String factoryId = (String) receivedObject;
            	Canvas objOutCanvas = localFileManager.read(factoryId);
            	
            	outObjectStream.writeObject(objOutCanvas);
            }

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}