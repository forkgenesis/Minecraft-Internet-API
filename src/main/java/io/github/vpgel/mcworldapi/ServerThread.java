package io.github.vpgel.mcworldapi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.MinecraftServer;

import static io.github.vpgel.mcworldapi.UsefulFunctions.*;

public class ServerThread extends Thread {
    MinecraftServer server;
    ServerSocket serverSocket;
    List<ClientThread> clients = new ArrayList<>();

    public ServerThread(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(2323);
            serverSocket.setReuseAddress(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MCWAPI.logger.info("Server started");

        while (!this.isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                MCWAPI.logger.info(String.format("Client %s connected", getAddress(socket)));
                ClientThread client = new ClientThread(socket, server);
                clients.add(client);
                client.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (ClientThread client: clients) {
            MCWAPI.logger.info(String.format("Client %s stopping", getAddress(client.socket)));
            client.interrupt();
        }
        
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MCWAPI.logger.info("Server stopped");
    }
}
