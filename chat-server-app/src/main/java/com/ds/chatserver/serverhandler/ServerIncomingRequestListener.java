package com.ds.chatserver.serverhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerIncomingRequestListener implements Runnable{
    private Integer socketPort;
    private ServerSocket serverSocket;
    private Server server;
    private static final Logger logger = LoggerFactory.getLogger(ServerIncomingRequestListener.class);

    public ServerIncomingRequestListener(Integer socketPort, Server server) throws IOException {
        this.server = server;
        this.socketPort = socketPort;
        serverSocket = new ServerSocket(socketPort);
    }
    @Override
    public void run() {
        logger.info("Running on port : {}", this.socketPort);
        while(true) {
            try {
//                logger.info("Waiting for new Server Connection ... ");
                Thread thread = new Thread(new ServerIncomingRequestHandler(serverSocket.accept(), this.server));
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
