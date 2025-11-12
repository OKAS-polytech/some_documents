package com.example.ftp.client;

import java.io.IOException;
import java.net.Socket;

public class Client {

    private Socket socket;

    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
    }

    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
