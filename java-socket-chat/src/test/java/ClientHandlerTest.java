import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.Socket;

/**
 * ClientHandlerクラスの単体テストです。
 */
class ClientHandlerTest {

    private MockChatServer mockServer;
    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        mockServer = new MockChatServer();
    }

    /**
     * ClientHandlerのrunメソッドが、クライアントとの一連の対話を正しく処理することをテストします。
     * (ニックネームの受信 -> メッセージのブロードキャスト -> 切断処理)
     */
    @Test
    void testRun() throws IOException {
        // --- 準備 ---
        // クライアントからの入力をシミュレート (ニックネームとメッセージ)
        String simulatedInput = "Jules\nこんにちは\n";
        testIn = new ByteArrayInputStream(simulatedInput.getBytes());

        // ClientHandlerからの出力をキャプチャ
        testOut = new ByteArrayOutputStream();

        // I/Oストリームを制御できるモックソケットを作成
        Socket mockSocket = new MockSocket(testIn, testOut);

        // テスト対象のClientHandlerを作成
        ClientHandler clientHandler = new ClientHandler(mockSocket, mockServer);

        // --- 実行 ---
        clientHandler.run();

        // --- 検証 ---
        // 1. ニックネーム入力要求がクライアントに送信されたか
        String outputToClient = testOut.toString();
        assertTrue(outputToClient.contains("ニックネームを入力してください:"), "クライアントにニックネームを要求するプロンプトが表示されるべきです。");

        // 2. 参加メッセージがブロードキャストされたか
        assertTrue(mockServer.wasBroadcastCalled(), "新しいクライアントの参加がブロードキャストされるべきです。");
        assertTrue(mockServer.getLastBroadcastMessage().contains("Jules がチャットに参加しました。"), "参加メッセージにニックネームが含まれるべきです。");

        // 3. チャットメッセージがブロードキャストされたか
        assertTrue(mockServer.getLastBroadcastMessage().contains("[Jules]: こんにちは"), "チャットメッセージがニックネーム付きでブロードキャストされるべきです。");

        // 4. クライアント切断処理が呼び出されたか
        assertTrue(mockServer.wasRemoveClientCalled(), "クライアント切断時にremoveClientが呼び出されるべきです。");
    }
}

/**
 * ChatServerの動作を模倣するモッククラス。
 */
class MockChatServer extends ChatServer {
    private boolean broadcastCalled = false;
    private boolean removeClientCalled = false;
    private String lastBroadcastMessage = "";

    public MockChatServer() {
        super(0); // ダミーのポート番号
    }

    @Override
    void broadcastMessage(String message, ClientHandler excludeUser) {
        this.broadcastCalled = true;
        this.lastBroadcastMessage = message;
    }

    @Override
    void removeClient(ClientHandler aUser) {
        this.removeClientCalled = true;
    }

    // --- テスト用ゲッター ---
    public boolean wasBroadcastCalled() { return broadcastCalled; }
    public boolean wasRemoveClientCalled() { return removeClientCalled; }
    public String getLastBroadcastMessage() { return lastBroadcastMessage; }
}

/**
 * Socketの動作を模倣するモッククラス。
 * InputStreamとOutputStreamをコンストラクタで指定できるようにする。
 */
class MockSocket extends Socket {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public MockSocket(InputStream in, OutputStream out) {
        this.inputStream = in;
        this.outputStream = out;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }
}
