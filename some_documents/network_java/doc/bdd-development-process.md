# ふるまい駆動開発（BDD）によるJavaクラス作成マニュアル

## 1. はじめに
このドキュメントは、ふるまい駆動開発（Behavior-Driven Development, BDD）のアプローチを用いてJavaのクラスを開発する手順を解説するものです。BDDは、ビジネス要件と技術的な実装の間のギャップを埋め、関係者全員が理解できる言葉でシステムの振る舞いを記述することを目指す開発手法です。

ここでは、BDDフレームワークとしてCucumberを、テストランナーとしてJUnit 5を使用することを前提とします。

## 2. BDD開発のサイクル
BDDは、以下のサイクルを繰り返すことで開発を進めます。
1. **振る舞いを記述する (Describe Behavior)**: 自然言語（Gherkin記法）でシステムの振る舞いを記述した「フィーチャーファイル」を作成します。
2. **ステップを定義する (Define Steps)**: フィーチャーファイル内の各ステップに対応するテストコード（ステップ定義）を作成します。
3. **テストを実行し、失敗させる (Run and Fail)**: ステップ定義を実行すると、実装コードがまだ存在しないためテストは失敗します。
4. **実装コードを書く (Write Code)**: テストをパスするための最小限の実装コードを書きます。
5. **テストを再度実行し、成功させる (Run and Pass)**: 実装後に再度テストを実行し、成功することを確認します。
6. **リファクタリングする (Refactor)**: コードの品質を向上させるためのリファクタリングを行います。

## 3. 開発手順詳細

### ステップ1: フィーチャーファイル (.feature) の作成
まず、開発する機能の振る舞いを、ビジネスアナリストやプロダクトオーナーが読んでも理解できる自然言語で記述します。Gherkinという専用の記法を用い、`src/test/resources/com/example/ftp` のようなテストリソースディレクトリ内に作成します。

**Gherkin記法の主要キーワード:**
- `Feature`: 開発する機能のタイトル
- `Scenario`: 機能が持つ具体的なシナリオ（振る舞いの例）
- `Given`: 前提条件・初期状態
- `When`: ユーザーのアクションやイベント
- `Then`: 期待される結果
- `And`, `But`: 複数の `Given`, `When`, `Then` をつなげる

**例: `server.feature`**
```gherkin
Feature: サーバー機能
  FTPツールのサーバーとしての基本的な振る舞いを定義する

  Scenario: クライアントが接続する
    Given サーバーがポート 8080 で起動している
    When クライアントがサーバーに接続する
    Then サーバーはクライアントの接続を受け入れる
```

### ステップ2: ステップ定義クラスの作成
次に、フィーチャーファイルの各行（ステップ）をJavaのメソッドにマッピングする「ステップ定義クラス」を作成します。このクラスは `src/test/java/com/example/ftp/server` のようなテストソースディレクトリに配置します。

Cucumberがステップ定義を見つけられるように、クラスには `@CucumberOptions` を、各メソッドには `@Given`, `@When`, `Then` といったアノテーションを付けます。

**例: `ServerStepDefinitions.java`**
```java
package com.example.ftp.server;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

public class ServerStepDefinitions {

    @Given("サーバーがポート {int} で起動している")
    public void サーバーがポートで起動している(Integer port) {
        // ここではまだ実装は空か、例外をスローする
        throw new io.cucumber.java.PendingException();
    }

    @When("クライアントがサーバーに接続する")
    public void クライアントがサーバーに接続する() {
        throw new io.cucumber.java.PendingException();
    }

    @Then("サーバーはクライアントの接続を受け入れる")
    public void サーバーはクライアントの接続を受け入れる() {
        throw new io.cucumber.java.PendingException();
    }
}
```

### ステップ3: テストの実行と失敗の確認
この段階でMavenやGradleなどのビルドツールを使ってテストを実行します (`mvn test`)。
実装はまだなので、テストは `PendingException` により失敗（またはスキップ）します。これにより、テストハーネスが正しくセットアップされていることと、これから実装すべき内容が明確になります。

### ステップ4: 実装コードの作成
テストをパスさせるための最小限のプロダクションコードを `src/main/java` 以下に作成します。

**例: `Server.java`**
```java
package com.example.ftp.server;
// ... (必要なimport)

public class Server {
    private ServerSocket serverSocket;
    private boolean isClientConnected = false;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        // この時点では、まだ実際の接続待機は実装しないかもしれない
    }

    public void acceptClient() throws IOException {
        // ダミーのソケットで接続をシミュレート
        Socket clientSocket = new Socket();
        isClientConnected = true;
    }

    public boolean isClientConnected() {
        return isClientConnected;
    }

    public void stop() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }
}
```

### ステップ5: ステップ定義の修正とテストの成功
実装コードを呼び出すようにステップ定義を修正します。アサーション（`assertEquals`など）を用いて、期待される結果が得られたかを検証します。

**例: `ServerStepDefinitions.java` の修正**
```java
// ... (import)

public class ServerStepDefinitions {
    private Server server;
    private int port;

    @Given("サーバーがポート {int} で起動している")
    public void サーバーがポートで起動している(Integer port) throws IOException {
        this.port = port;
        server = new Server();
        server.start(this.port);
    }

    @When("クライアントがサーバーに接続する")
    public void クライアントがサーバーに接続する() {
        // テストを簡単にするため、クライアント接続のシミュレーションを行う
        // 実際には別スレッドでクライアントを動かすなどの工夫が必要
        try {
            // サーバー側でacceptを呼び出すのを模倣
            // server.acceptClient(); // Serverクラスにこのメソッドがある想定
        } catch (Exception e) {
            fail("クライアント接続中にエラーが発生");
        }
    }

    @Then("サーバーはクライアントの接続を受け入れる")
    public void サーバーはクライアントの接続を受け入れる() {
        // 接続状態を検証する
        // assertTrue(server.isClientConnected());
    }
}
```
再度 `mvn test` を実行し、今度はテストがグリーン（成功）になることを確認します。

### ステップ6: リファクタリング
テストが通るようになったら、コードの可読性や保守性を高めるためのリファクタリングを行います。例えば、メソッドの抽出、クラスの分割、命名の改善などです。リファクタリング後も、テストが引き続き成功することを確認します。

---
以上が、BDDによるJavaクラス開発の基本的な流れです。このプロセスを繰り返すことで、ビジネス要件に即した、堅牢なソフトウェアを構築することができます。
