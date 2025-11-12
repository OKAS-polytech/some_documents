package com.example.ftp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean isRunning = false;
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private java.util.function.Consumer<String> logger = System.out::println;

    public void setLogger(java.util.function.Consumer<String> logger) {
        this.logger = logger;
    }

    public java.util.function.Consumer<String> getLogger() {
        return logger;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        logger.accept("サーバーがポート " + port + " で起動しました。");

        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.accept("新しいクライアントが接続しました: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                pool.execute(handler);
            } catch (IOException e) {
                if (!isRunning) {
                    System.out.println("サーバーが停止しました。");
                    break;
                }
                System.err.println("クライアント接続の受付中にエラーが発生しました: " + e.getMessage());
            }
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) { // 送信者自身には送らない
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void stop() throws IOException {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        pool.shutdownNow();
    }

    // For testing purposes
    public void setClients(List<ClientHandler> clients) {
        this.clients = clients;
    }
}
