package com.example.ftp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
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
                if ("LIST".equals(line)) {
                    handleListCommand();
                } else if (line.startsWith("UPLOAD:")) {
                    String fileName = line.substring(7).trim();
                    handleUploadCommand(fileName);
                } else if (line.startsWith("DOWNLOAD:")) {
                    String fileName = line.substring(9).trim();
                    handleDownloadCommand(fileName);
                } else if (line.startsWith("MSG:")) {
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

    private void handleListCommand() {
        java.io.File dir = new java.io.File(server.getSharedDirectory());
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            StringBuilder fileList = new StringBuilder("FILE_LIST:");
            for (java.io.File file : files) {
                if (file.isFile()) {
                    fileList.append(file.getName()).append(",");
                }
            }
            // 最後のカンマを削除
            if (fileList.length() > "FILE_LIST:".length()) {
                fileList.deleteCharAt(fileList.length() - 1);
            }
            sendMessage(fileList.toString());
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private void handleUploadCommand(String fileName) {
        new Thread(() -> {
            try (ServerSocket dataServerSocket = new ServerSocket(0)) { // 0で空きポートを自動選択
                int dataPort = dataServerSocket.getLocalPort();
                sendMessage("PORT:" + dataPort); // データポートをクライアントに通知

                try (Socket dataSocket = dataServerSocket.accept();
                     java.io.InputStream in = dataSocket.getInputStream();
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(
                             java.nio.file.Paths.get(server.getSharedDirectory(), fileName).toString())) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    server.getLogger().accept("File " + fileName + " received successfully.");
                }
            } catch (IOException e) {
                server.getLogger().accept("File upload error for " + fileName + ": " + e.getMessage());
            }
        }).start();
    }

    private void handleDownloadCommand(String fileName) {
        new Thread(() -> {
            try {
                java.nio.file.Path filePath = java.nio.file.Paths.get(server.getSharedDirectory(), fileName);
                if (!java.nio.file.Files.exists(filePath)) {
                    sendMessage("ERROR:File not found");
                    return;
                }

                try (ServerSocket dataServerSocket = new ServerSocket(0)) {
                    int dataPort = dataServerSocket.getLocalPort();
                    sendMessage("PORT:" + dataPort);

                    try (Socket dataSocket = dataServerSocket.accept();
                         java.io.FileInputStream fis = new java.io.FileInputStream(filePath.toFile());
                         java.io.OutputStream out = dataSocket.getOutputStream()) {

                        fis.transferTo(out);
                        server.getLogger().accept("File " + fileName + " sent successfully.");
                    }
                }
            } catch (IOException e) {
                server.getLogger().accept("File download error for " + fileName + ": " + e.getMessage());
            }
        }).start();
    }

    // For testing purposes
    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }
}
