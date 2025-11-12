# テスト駆動開発（TDD）によるJavaクラス作成マニュアル

## 1. はじめに
このドキュメントは、テスト駆動開発（Test-Driven Development, TDD）のアプローチを用いてJavaのクラスを開発する手順を解説するものです。TDDは、「まずテストを書き、そのテストをパスする最小限のコードを実装し、その後コードを整理する」という短いサイクルを繰り返すことで、高品質なソフトウェアを効率的に開発する手法です。

このマニュアルでは、テストフレームワークとしてJUnit 5を使用することを前提とします。

## 2. TDDの基本サイクル：Red / Green / Refactor
TDDの核心は、以下の3つのフェーズを数分単位の短いサイクルで繰り返すことにあります。

1.  **Redフェーズ：失敗するテストを書く**
    - これから実装する機能に対する、小さなテストケースを一つ書きます。
    - この時点では実装コードが存在しないため、このテストはコンパイルエラーになるか、実行して失敗（Red）します。

2.  **Greenフェーズ：テストをパスさせる**
    - Redフェーズで書いたテストを成功させるための、**最小限の**プロダクションコードを書きます。
    - この段階では、コードの綺麗さや効率は度外視し、とにかくテストをパスさせること（Green）だけを目的とします。

3.  **Refactorフェーズ：コードをクリーンにする**
    - テストが成功している状態を維持したまま、プロダクションコードの品質を向上させます。
    - 重複の排除、命名の改善、複雑なロジックの単純化など、リファクタリングを行います。
    - テストコード自体もリファクタリングの対象となります。

## 3. 開発手順詳細

### ステップ1: テストクラスの準備
まず、これから作成するクラス（例: `Calculator`）に対応するテストクラス（`CalculatorTest`）を `src/test/java` ディレクトリに作成します。

### ステップ2 (Red): 失敗するテストケースの追加
`Calculator`に`add`メソッドを追加することを想定し、`CalculatorTest.java`にテストケースを一つ追加します。

**例: `CalculatorTest.java`**
```java
package com.example.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {

    @Test
    void testAddTwoNumbers() {
        Calculator calculator = new Calculator();
        int result = calculator.add(2, 3);
        assertEquals(5, result, "2 + 3 should equal 5");
    }
}
```
この時点では`Calculator`クラスが存在しないため、このコードはコンパイルエラーになります。これが**Redフェーズ**です。

### ステップ3: テストの実行と失敗の確認
MavenやIDEを使ってテストを実行します。`Calculator`クラスや`add`メソッドが存在しないため、コンパイルエラーが発生し、テストが失敗することを確認します。

```sh
# Mavenでテストを実行
mvn test
# -> COMPILATION ERROR
```

### ステップ4 (Green): テストをパスする最小限のコードを実装
コンパイルエラーを解消し、テストをパスさせるための最小限のコードを `src/main/java` 以下に作成します。

**例: `Calculator.java`**
```java
package com.example.math;

public class Calculator {
    // とにかくテストを通すため、ハードコードした値を返す
    public int add(int a, int b) {
        return 5;
    }
}
```
この実装は明らかに不完全ですが、「2と3を足すと5になる」というテストケースはパスします。これが**Greenフェーズ**です。

### ステップ5: テストの再実行と成功の確認
再度`mvn test`を実行します。今度はテストが成功（Green）することを確認します。

### ステップ6 (Refactor): 実装のリファクタリング
テストが成功しているので、安心してコードをより汎用的な形にリファクタリングできます。

**例: `Calculator.java` の修正**
```java
package com.example.math;

public class Calculator {
    // 正しいロジックに修正
    public int add(int a, int b) {
        return a + b;
    }
}
```
リファクタリング後、再度テストを実行し、依然として成功することを確認します。これにより、リファクタリングによって既存の機能が壊れていないことが保証されます。

### ステップ7: 次のサイクルへ
`add`機能の実装が終わったら、次の機能（例: 引き算）のために、また新しい失敗するテストケースを書くところからサイクルを再開します。

---
以上が、TDDによるJavaクラス開発の基本的な流れです。この小さな成功体験を積み重ねることで、自信を持って、かつ品質を高く保ちながら開発を進めることができます。
