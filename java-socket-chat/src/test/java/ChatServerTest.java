import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatServerクラスの単体テストです。
 */
class ChatServerTest {

    private ChatServer chatServer;
    private List<String> receivedMessages; // メッセージを受信したクライアントを記録するリスト

    @BeforeEach
    void setUp() {
        chatServer = new ChatServer(8080);
        receivedMessages = new ArrayList<>();
    }

    /**
     * ClientHandlerをモック化せずに、sendMessageメソッドだけをオーバーライドする
     * テスト用のClientHandlerインスタンスを作成します。
     */
    private ClientHandler createTestClientHandler(String nickname) {
        // ClientHandlerのテスト用コンストラクタを使用
        return new ClientHandler(nickname, chatServer) {
            @Override
            void sendMessage(String message) {
                // メッセージを送信する代わりに、リストに受信者ニックネームとメッセージを記録
                receivedMessages.add(getNickname() + ":" + message);
            }
        };
    }

    /**
     * broadcastMessageメソッドが、送信者を除くすべてのクライアントにメッセージを送信することをテストします。
     */
    @Test
    void testBroadcastMessage() {
        // テスト用のクライアントハンドラを作成
        ClientHandler sender = createTestClientHandler("sender");
        ClientHandler receiver1 = createTestClientHandler("receiver1");
        ClientHandler receiver2 = createTestClientHandler("receiver2");

        // サーバーにクライアントを追加
        chatServer.addClientForTest(sender);
        chatServer.addClientForTest(receiver1);
        chatServer.addClientForTest(receiver2);

        // ブロードキャストを実行
        String testMessage = "こんにちは！";
        chatServer.broadcastMessage(testMessage, sender);

        // 結果を検証
        assertEquals(2, receivedMessages.size(), "メッセージは2人の受信者にのみ送信されるはずです。");
        assertTrue(receivedMessages.contains("receiver1:" + testMessage), "受信者1はメッセージを受信するはずです。");
        assertTrue(receivedMessages.contains("receiver2:" + testMessage), "受信者2はメッセージを受信するはずです。");
        assertFalse(receivedMessages.stream().anyMatch(m -> m.startsWith("sender:")), "送信者自身はメッセージを受信しないはずです。");
    }

    /**
     * removeClientメソッドが、クライアントリストから正しくクライアントを削除することをテストします。
     */
    @Test
    void testRemoveClient() {
        ClientHandler client1 = createTestClientHandler("client1");
        chatServer.addClientForTest(client1);

        assertEquals(1, chatServer.getClientCountForTest(), "クライアント追加後、カウントは1であるべきです。");

        chatServer.removeClient(client1);

        assertEquals(0, chatServer.getClientCountForTest(), "クライアント削除後、カウントは0であるべきです。");
    }
}
