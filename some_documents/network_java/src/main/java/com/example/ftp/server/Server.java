package com.example.ftp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean isRunning = false;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        System.out.println("サーバーがポート " + port + " で起動しました。");

        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新しいクライアントが接続しました: " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                if (!isRunning) {
                    System.out.println("サーバーが停止しました。");
                    break;
                }
                System.err.println("クライアント接続の受付中にエラーが発生しました: " + e.getMessage());
            }
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        pool.shutdownNow();
    }
}
