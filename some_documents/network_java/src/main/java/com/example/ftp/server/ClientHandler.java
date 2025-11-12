package com.example.ftp.server;

import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        // ここにクライアントとの通信処理を実装する
        System.out.println(clientSocket.getInetAddress() + " との通信スレッドを開始します。");
        // この段階では何もしない
    }
}
