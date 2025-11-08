import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 個々のクライアント接続を処理するためのスレッドです。
 * クライアントからのメッセージを読み取り、サーバーにブロードキャストを依頼します。
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    protected String nickname; // テストでアクセスできるようprotectedに変更

    /**
     * 新しいクライアントハンドラを初期化します。
     * @param socket クライアントに接続されているソケット
     * @param server ChatServerインスタンスへの参照
     */
    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    /**
     * [テスト用] モックオブジェクトと共に使用するためのコンストラクタ。
     * @param nickname このハンドラのニックネーム
     * @param server ChatServerインスタンス
     */
    ClientHandler(String nickname, ChatServer server) {
        this.socket = null; // 実際の通信はしないためnull
        this.server = server;
        this.nickname = nickname;
    }

    /**
     * スレッドのメインロジックです。
     * クライアントのニックネームを読み取り、切断されるまでメッセージを継続的に待ち受けます。
     */
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("ニックネームを入力してください:");
            nickname = in.readLine();
            server.broadcastMessage(nickname + " がチャットに参加しました。", this);

            String message;
            while ((message = in.readLine()) != null) {
                server.broadcastMessage("[" + nickname + "]: " + message, this);
            }

        } catch (IOException e) {
            // クライアントが切断した際の接続リセットは想定内です
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * このクライアントにメッセージを送信します。
     * @param message 送信するメッセージ
     */
    void sendMessage(String message) {
        out.println(message);
    }

    /**
     * このクライアントのニックネームを取得します。
     * @return クライアントのニックネーム
     */
    public String getNickname() {
        return nickname;
    }
}
