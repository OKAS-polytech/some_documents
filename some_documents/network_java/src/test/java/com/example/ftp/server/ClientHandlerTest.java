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

    @Test
    void testHandleListCommand() throws IOException {
        // テスト用の共有ディレクトリとファイルを作成
        java.nio.file.Path sharedDir = java.nio.file.Paths.get("target/shared");
        java.nio.file.Files.createDirectories(sharedDir);
        java.nio.file.Files.createFile(sharedDir.resolve("testfile1.txt"));
        java.nio.file.Files.createFile(sharedDir.resolve("testfile2.txt"));

        Server server = new Server();
        server.setSharedDirectory(sharedDir.toString());

        MockSocket clientSocket = new MockSocket("LIST\n");
        ClientHandler handler = new ClientHandler(clientSocket, server);

        handler.run();

        String output = clientSocket.getOutput();
        assertTrue(output.startsWith("FILE_LIST:"), "ファイルリストのプレフィックスが正しいはず");
        assertTrue(output.contains("testfile1.txt"), "ファイル1が含まれているはず");
        assertTrue(output.contains("testfile2.txt"), "ファイル2が含まれているはず");

        // クリーンアップ
        java.nio.file.Files.delete(sharedDir.resolve("testfile1.txt"));
        java.nio.file.Files.delete(sharedDir.resolve("testfile2.txt"));
        java.nio.file.Files.delete(sharedDir);
    }

    @Test
    void testHandleUploadCommand() throws IOException, InterruptedException {
        // テスト用の共有ディレクトリ
        java.nio.file.Path sharedDir = java.nio.file.Paths.get("target/shared_upload");
        java.nio.file.Files.createDirectories(sharedDir);

        Server server = new Server();
        server.setSharedDirectory(sharedDir.toString());

        final java.util.concurrent.CountDownLatch portLatch = new java.util.concurrent.CountDownLatch(1);
        MockSocket clientSocket = new MockSocket("UPLOAD:upload_test.txt\n");

        ClientHandler handler = new ClientHandler(clientSocket, server) {
            @Override
            public void sendMessage(String message) {
                super.sendMessage(message);
                if (message.startsWith("PORT:")) {
                    portLatch.countDown();
                }
            }
        };
        handler.setWriter(new PrintWriter(clientSocket.getOutputStream(), true));

        handler.run();

        // PORT応答が送信されるまで最大3秒待つ
        assertTrue(portLatch.await(3, java.util.concurrent.TimeUnit.SECONDS), "PORT応答がタイムアウトしました");

        String serverResponse = clientSocket.getOutput().trim();
        assertTrue(serverResponse.startsWith("PORT:"), "サーバーがデータポートを通知するはず: Got " + serverResponse);
        int dataPort = Integer.parseInt(serverResponse.substring(5));

        // データポートに接続してファイル内容を送信
        try (Socket dataSocket = new Socket("localhost", dataPort)) {
            dataSocket.getOutputStream().write("Hello Upload".getBytes());
        }

        Thread.sleep(200); // ファイル書き込み完了を待つ

        // アップロードされたファイルが存在し、内容が正しいことを確認
        java.nio.file.Path uploadedFile = sharedDir.resolve("upload_test.txt");
        assertTrue(java.nio.file.Files.exists(uploadedFile), "ファイルがアップロードされているはず");
        String content = new String(java.nio.file.Files.readAllBytes(uploadedFile));
        assertEquals("Hello Upload", content, "ファイルの内容が正しいはず");

        // クリーンアップ
        java.nio.file.Files.delete(uploadedFile);
        java.nio.file.Files.delete(sharedDir);
    }

    @Test
    void testHandleDownloadCommand() throws IOException, InterruptedException {
        // テスト用の共有ディレクトリとファイルを作成
        java.nio.file.Path sharedDir = java.nio.file.Paths.get("target/shared_download");
        java.nio.file.Files.createDirectories(sharedDir);
        java.nio.file.Path testFile = sharedDir.resolve("download_test.txt");
        java.nio.file.Files.write(testFile, "Hello Download".getBytes());

        Server server = new Server();
        server.setSharedDirectory(sharedDir.toString());

        MockSocket clientSocket = new MockSocket("DOWNLOAD:download_test.txt\n");
        ClientHandler handler = new ClientHandler(clientSocket, server);

        Thread handlerThread = new Thread(handler);
        handlerThread.start();

        Thread.sleep(200);

        String serverResponse = clientSocket.getOutput();
        assertTrue(serverResponse.startsWith("PORT:"), "サーバーがデータポートを通知するはず");
        int dataPort = Integer.parseInt(serverResponse.substring(5).trim());

        // データポートに接続してファイル内容を受信
        ByteArrayOutputStream receivedData = new ByteArrayOutputStream();
        try (Socket dataSocket = new Socket("localhost", dataPort)) {
            dataSocket.getInputStream().transferTo(receivedData);
        }

        handlerThread.join();

        assertEquals("Hello Download", receivedData.toString(), "ダウンロードしたファイルの内容が正しいはず");

        // クリーンアップ
        java.nio.file.Files.delete(testFile);
        java.nio.file.Files.delete(sharedDir);
    }
}
