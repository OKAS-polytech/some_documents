package com.example.ftp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Client {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private volatile boolean isRunning;

    private CountDownLatch portLatch;
    private int dataPort;

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
                    if (line.startsWith("PORT:")) {
                        this.dataPort = Integer.parseInt(line.substring(5).trim());
                        if (portLatch != null) {
                            portLatch.countDown();
                        }
                    } else {
                        onMessageReceived.accept(line);
                    }
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

    public void uploadFile(String filePath) throws IOException {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        portLatch = new CountDownLatch(1);
        sendMessage("UPLOAD:" + file.getName());

        try {
            if (!portLatch.await(5, TimeUnit.SECONDS)) {
                throw new IOException("Server did not respond with a data port in time.");
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while waiting for data port.", e);
        }

        try (Socket dataSocket = new Socket(socket.getInetAddress().getHostName(), this.dataPort);
             java.io.FileInputStream fis = new java.io.FileInputStream(file);
             java.io.OutputStream out = dataSocket.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public void downloadFile(String fileName, String savePath) throws IOException {
        portLatch = new CountDownLatch(1);
        sendMessage("DOWNLOAD:" + fileName);

        try {
            if (!portLatch.await(5, TimeUnit.SECONDS)) {
                throw new IOException("Server did not respond with a data port in time.");
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while waiting for data port.", e);
        }

        try (Socket dataSocket = new Socket(socket.getInetAddress().getHostName(), this.dataPort);
             java.io.InputStream in = dataSocket.getInputStream();
             java.io.FileOutputStream fos = new java.io.FileOutputStream(
                     java.nio.file.Paths.get(savePath, fileName).toString())) {

            in.transferTo(fos);
        }
    }
}
