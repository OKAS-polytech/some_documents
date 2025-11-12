package com.example.ftp.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.Socket;

public class ServerTest {

    @Test
    void testServerAcceptsClientConnection() {
        // サーバーを別スレッドで起動
        int port = 8080;
        Server server = new Server();
        Thread serverThread = new Thread(() -> {
            try {
                server.start(port);
            } catch (IOException e) {
                fail("サーバーの起動に失敗しました: " + e.getMessage());
            }
        });
        serverThread.start();

        // クライアントからの接続を試みる
        try (Socket clientSocket = new Socket("localhost", port)) {
            assertTrue(clientSocket.isConnected(), "クライアントがサーバーに接続できるはず");
        } catch (IOException e) {
            fail("クライアントの接続に失敗しました: " + e.getMessage());
        } finally {
            // サーバーを停止
            try {
                server.stop();
                serverThread.join(); // スレッドの終了を待つ
            } catch (IOException | InterruptedException e) {
                // ignore
            }
        }
    }
}
