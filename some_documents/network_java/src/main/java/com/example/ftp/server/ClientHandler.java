package com.example.ftp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Server server;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        this.username = "user" + (int)(Math.random() * 1000); // 仮のユーザー名
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while ((line = reader.readLine()) != null) {
                server.getLogger().accept("Received from " + username + ": " + line);
                if (line.startsWith("MSG:")) {
                    String message = line.substring(4).trim();
                    server.broadcastMessage("BROADCAST:[" + username + "]:" + message, this);
                }
            }
        } catch (IOException e) {
            server.getLogger().accept(username + "との通信でエラーが発生しました: " + e.getMessage());
        } finally {
            server.removeClient(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                // ignore
            }
            server.getLogger().accept(username + "が切断しました。");
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    // For testing purposes
    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }
}
