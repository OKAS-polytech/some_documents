package com.example.ftp.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientTest {

    private ServerSocket dummyServerSocket;
    private Thread serverThread;
    private final int port = 8081;
    private Socket acceptedSocket;
    private CountDownLatch serverReadyLatch;
    private CountDownLatch clientAcceptedLatch;

    @BeforeEach
    void setUp() throws InterruptedException {
        serverReadyLatch = new CountDownLatch(1);
        clientAcceptedLatch = new CountDownLatch(1);

        serverThread = new Thread(() -> {
            try {
                dummyServerSocket = new ServerSocket(port);
                serverReadyLatch.countDown(); // サーバーの準備ができたことを通知
                acceptedSocket = dummyServerSocket.accept();
                clientAcceptedLatch.countDown(); // クライアントを受け入れたことを通知
            } catch (IOException e) {
                // ignore
            }
        });
        serverThread.start();

        // サーバーがポートをリッスンし始めるのを待つ
        if (!serverReadyLatch.await(3, TimeUnit.SECONDS)) {
            fail("サーバーの起動がタイムアウトしました。");
        }
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        if (acceptedSocket != null && !acceptedSocket.isClosed()) {
            acceptedSocket.close();
        }
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
        assertThrows(ConnectException.class, () -> {
            client.connect("localhost", 9999);
        }, "存在しないサーバーには接続できないはず");
    }

    @Test
    void testClientSendMessage() throws IOException, InterruptedException {
        Client client = new Client();
        client.connect("localhost", port);

        // サーバーがクライアントを受け入れるまで待つ
        if (!clientAcceptedLatch.await(3, TimeUnit.SECONDS)) {
            fail("サーバーがクライアント接続を受け入れるのがタイムアウトしました。");
        }

        assertNotNull(acceptedSocket, "サーバーはクライアント接続を受け入れているはず");
        BufferedReader serverReader = new BufferedReader(new InputStreamReader(acceptedSocket.getInputStream()));

        client.sendMessage("MSG:Hello Server");

        assertEquals("MSG:Hello Server", serverReader.readLine(), "サーバーが正しいメッセージを受信できるはず");

        client.disconnect();
    }
}
