import java.util.Scanner;

/**
 * チャットアプリケーションを起動するためのメインクラスです。
 * ユーザーに対話形式でサーバーまたはクライアントの起動を選択させます。
 */
public class Main {

    /**
     * アプリケーションのメインエントリーポイントです。
     * @param args コマンドライン引数（このアプリケーションでは使用しません）
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("起動するモードを選択してください。");
        System.out.print(" (1: サーバー, 2: クライアント): ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                startServer(scanner);
                break;
            case "2":
                startClient(scanner);
                break;
            default:
                System.out.println("無効な選択です。'1' または '2' を入力してください。");
                break;
        }

        scanner.close();
    }

    /**
     * サーバーを起動するための情報をユーザーから取得し、ChatServerを起動します。
     * @param scanner ユーザー入力を読み取るためのScannerインスタンス
     */
    private static void startServer(Scanner scanner) {
        System.out.print("サーバーが使用するポート番号を入力してください (例: 8080): ");
        String port = scanner.nextLine();
        System.out.println(port + "番ポートでサーバーを起動します...");
        ChatServer.main(new String[]{port});
    }

    /**
     * クライアントを起動するための情報をユーザーから取得し、ChatClientを起動します。
     * @param scanner ユーザー入力を読み取るためのScannerインスタンス
     */
    private static void startClient(Scanner scanner) {
        System.out.print("接続するサーバーのホスト名またはIPアドレスを入力してください (例: 127.0.0.1): ");
        String host = scanner.nextLine();
        System.out.print("接続するサーバーのポート番号を入力してください (例: 8080): ");
        String port = scanner.nextLine();
        System.out.println(host + ":" + port + " に接続します...");
        ChatClient.main(new String[]{host, port});
    }
}
