import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * チャットアプリケーションのクライアントサイドを管理するクラスです。
 * サーバーへの接続、メッセージの送信、およびサーバーからのメッセージ受信を担当します。
 */
public class ChatClient {
    private final String hostname;
    private final int port;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * 指定されたホスト名とポート番号でクライアントを初期化します。
     * @param hostname 接続するサーバーのホスト名
     * @param port 接続するサーバーのポート番号
     */
    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * アプリケーションのエントリーポイントです。
     * コマンドライン引数からホスト名とポート番号を取得し、クライアントを起動します。
     * @param args コマンドライン引数。ホスト名とポート番号を期待します。
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("使用法: java ChatClient <host> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        new ChatClient(hostname, port).execute();
    }

    /**
     * サーバーへの接続を実行し、メッセージの送受信を開始します。
     * サーバーからのメッセージ受信は、ユーザー入力をブロックしないように別のスレッドで処理されます。
     */
    public void execute() {
        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("チャットサーバーに接続しました");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // サーバーからのメッセージを読み取るためのスレッド
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("サーバーからの読み取り中にエラーが発生しました: " + e.getMessage());
                }
            }).start();

            // ユーザー入力を読み取りサーバーに送信するためのメインスレッド
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                out.println(userInput);
            }

        } catch (UnknownHostException ex) {
            System.out.println("サーバーが見つかりません: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/Oエラー: " + ex.getMessage());
        }
    }
}
