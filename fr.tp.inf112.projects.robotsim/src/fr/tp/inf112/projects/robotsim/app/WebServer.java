package fr.tp.inf112.projects.robotsim.app;
import java.net.ServerSocket;
import java.net.Socket;

import fr.tp.inf112.projects.robotsim.serverutils.RequestProcessor;

public class WebServer {
	static int port = 8081;
    public static void main(String[] args) throws Exception {
        try (
            ServerSocket serverSocket = new ServerSocket(port); 
        ){
            do{
                try {
                    Socket socket = serverSocket.accept();
                    Runnable reqProcessor = new RequestProcessor(socket);
                    new Thread(reqProcessor).start(); 
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }while(true);
        }
    }
}

