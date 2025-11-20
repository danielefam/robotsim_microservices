package fr.tp.inf112.projects.robotsim.serverutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.Canvas;
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
            OutputStream outStr = socket.getOutputStream();
        	ObjectOutputStream outObjectStream = new ObjectOutputStream(outStr);
        ){
            outObjectStream.flush();
            InputStream inpStr = socket.getInputStream();
        	ObjectInputStream inpObjectStream = new ObjectInputStream(inpStr);
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