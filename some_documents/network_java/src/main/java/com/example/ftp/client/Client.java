package com.example.ftp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class Client {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private volatile boolean isRunning;

    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public void startListening(Consumer<String> onMessageReceived) {
        isRunning = true;
        listenerThread = new Thread(() -> {
            try {
                String line;
                while (isRunning && (line = reader.readLine()) != null) {
                    onMessageReceived.accept(line);
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("サーバーとの接続が切れました: " + e.getMessage());
                }
            }
        });
        listenerThread.start();
    }

    public void stopListening() {
        isRunning = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    public void disconnect() throws IOException {
        stopListening();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
