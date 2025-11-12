package com.example.ftp.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandlerTest {

    // Socketの振る舞いを模倣するスタブクラス
    private static class MockSocket extends Socket {
        private final ByteArrayInputStream in;
        private final ByteArrayOutputStream out;

        public MockSocket(String input) {
            this.in = new ByteArrayInputStream(input.getBytes());
            this.out = new ByteArrayOutputStream();
        }

        @Override
        public java.io.InputStream getInputStream() {
            return in;
        }

        @Override
        public java.io.OutputStream getOutputStream() {
            return out;
        }

        public String getOutput() {
            return out.toString();
        }
    }

    @Test
    void testHandleMessageCommand() throws IOException {
        // サーバーインスタンスを作成し、クライアントリストを持たせる
        Server server = new Server();
        List<ClientHandler> clients = new ArrayList<>();
        server.setClients(clients);

        // 2つのクライアントハンドラを準備
        MockSocket clientSocket1 = new MockSocket("MSG:Hello from client 1\n");
        ClientHandler handler1 = new ClientHandler(clientSocket1, server);
        handler1.setWriter(new PrintWriter(clientSocket1.getOutputStream(), true));

        MockSocket clientSocket2 = new MockSocket(""); // こちらは受信側
        ClientHandler handler2 = new ClientHandler(clientSocket2, server);
        handler2.setWriter(new PrintWriter(clientSocket2.getOutputStream(), true));

        clients.add(handler1);
        clients.add(handler2);

        // handler1がメッセージを処理する
        handler1.run();

        // handler2がメッセージを受信していることを確認
        // ブロードキャストメッセージのフォーマットを "BROADCAST:[...]:Hello from client 1" と想定
        assertTrue(clientSocket2.getOutput().startsWith("BROADCAST:"), "メッセージがブロードキャストされるはず");
        assertTrue(clientSocket2.getOutput().contains("Hello from client 1"), "メッセージ内容が正しいはず");
    }
}
