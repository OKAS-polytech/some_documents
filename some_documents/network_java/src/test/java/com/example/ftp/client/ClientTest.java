package com.example.ftp.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;


public class ClientTest {

    private ServerSocket dummyServerSocket;
    private Thread serverThread;
    private final int port = 8081;

    @BeforeEach
    void setUp() {
        // 各テストの前にダミーサーバーを起動
        serverThread = new Thread(() -> {
            try {
                dummyServerSocket = new ServerSocket(port);
                dummyServerSocket.accept(); // 接続を一度だけ受け付ける
            } catch (IOException e) {
                // ignore
            }
        });
        serverThread.start();
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        // 各テストの後にサーバーを停止
        if (dummyServerSocket != null && !dummyServerSocket.isClosed()) {
            dummyServerSocket.close();
        }
        serverThread.join();
    }

    @Test
    void testClientConnectsToServer() {
        Client client = new Client();
        assertDoesNotThrow(() -> {
            client.connect("localhost", port);
            client.disconnect();
        }, "クライアントはサーバーに接続できるはず");
    }

    @Test
    void testClientFailsToConnectToInvalidServer() {
        Client client = new Client();
        // 存在しないポートに接続を試みる
        assertThrows(ConnectException.class, () -> {
            client.connect("localhost", 9999);
        }, "存在しないサーバーには接続できないはず");
    }
}
