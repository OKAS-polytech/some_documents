import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * チャットアプリケーションのサーバーサイドを管理するクラスです。
 * クライアントからの接続を待ち受け、各クライアントを個別のスレッドで処理します。
 */
public class ChatServer {
    private final int port;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();

    /**
     * 指定されたポート番号でサーバーを初期化します。
     * @param port サーバーがリッスンするポート番号
     */
    public ChatServer(int port) {
        this.port = port;
    }

    /**
     * アプリケーションのエントリーポイントです。
     * コマンドライン引数からポート番号を取得し、サーバーを起動します。
     * @param args コマンドライン引数。ポート番号を期待します。
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("使用法: java ChatServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new ChatServer(port).start();
    }

    /**
     * サーバーを起動し、クライアントからの接続を待ち受けます。
     * 新しい接続があるたびに、そのクライアントを処理するための新しいスレッドを開始します。
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("チャットサーバーがポート " + port + " で待機しています");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("新しいクライアントが接続しました");

                ClientHandler newUser = new ClientHandler(socket, this);
                clientHandlers.add(newUser);
                new Thread(newUser).start();
            }

        } catch (IOException ex) {
            System.out.println("サーバーでエラーが発生しました: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * 指定されたクライアントを除く、接続されているすべてのクライアントにメッセージをブロードキャストします。
     * @param message 送信するメッセージ
     * @param excludeUser メッセージ送信から除外するクライアント
     */
    void broadcastMessage(String message, ClientHandler excludeUser) {
        for (ClientHandler aUser : clientHandlers) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    /**
     * クライアントが切断した際に、クライアントのリストから削除します。
     * @param aUser 削除するクライアントハンドラ
     */
    void removeClient(ClientHandler aUser) {
        clientHandlers.remove(aUser);
        System.out.println("クライアントが切断しました: " + aUser.getNickname());
    }
}
