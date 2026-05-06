# Java + JetBrains Koog Sample

JavaからJetBrains Koogを使って、簡単なAIエージェントを作るサンプルプロジェクトです。

このプロジェクトでは、次の2つのサンプルを用意しています。

- `KoogJavaExample.java`
  - Koogの基本的な使い方を確認する最小サンプル
- `MultiToolAgent.java`
  - 天気取得・翻訳・計算の複数ツールを扱うマルチツールAIエージェント

## 概要

JetBrains Koogは、LLMを使ったAIエージェント構築用のJVM向けフレームワークです。

このサンプルでは、JavaからKoogを利用し、以下のような処理を行うAIエージェントを作成します。

- ユーザー入力を受け取る
- 必要に応じてツールを呼び出す
- ツールの結果をもとに応答する

## 作るもの

最終的には、コンソール上で次のように会話できるAIエージェントを作る想定です。

```text
マルチツールAIエージェントへようこそ！（終了するには 'exit' と入力）
あなた: 東京の天気は？
エージェント: 東京の現在の気温は18.3℃、天気は曇りです。

あなた: それを英語にして
エージェント: The current temperature in Tokyo is 18.3°C and the weather is cloudy.

あなた: 25*12は？
エージェント: 25*12 = 300.0
```

ここで大事なのは、AIエージェントが単に文章を返すだけではなく、必要に応じてJava側で定義したツールを呼び出す点です。

たとえば、

- 天気を聞かれたら天気APIを呼び出す
- 翻訳を頼まれたら翻訳APIを呼び出す
- 計算を頼まれたら計算ツールを呼び出す

というように、ユーザーの入力内容に応じて処理を切り替えます。

## 使用技術

- Java 25
- Maven 4.0.0-rc-5
- Koog (ai.koog:koog-agents-jvm:0.7.3)
- Jackson
- Apache HttpClient 5
- mXparser
- OpenWeatherMap API
- MyMemory Translated API

## ファイル構成

今回はMavenプロジェクトとして、次のような構成にします。

```text
koog-java-sample/
├── pom.xml
├── README.md
├── mvn.cmd
├── .gitignore
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── koog/
                        ├── KoogJavaExample.java
                        └── MultiToolAgent.java
```

`KoogJavaExample.java` は基本動作確認用、`MultiToolAgent.java` は複数ツールを扱う本命のサンプルとして使います。

## セットアップ

### 1. リポジトリをクローンする

```bash
git clone https://github.com/実際のユーザー名/実際のリポジトリ名.git
cd koog-java-sample
```

### 2. Mavenで依存関係を取得する

```bash
mvn clean compile
```

> `mvn` が PATH に登録されていない場合は、プロジェクトルートに作成した `mvn.cmd` を使って実行できます。
>
> ```bash
> .\mvn.cmd clean compile
> ```

---

## APIキーの設定

`MultiToolAgent.java` では、次の2つのAPIキーを環境変数から読み込みます。

- `OPENAI_API_KEY` - KoogのOpenAIプロンプト実行に使用
- `OPENWEATHERMAP_API_KEY` - OpenWeatherMapの天気情報取得に使用

APIキーはソースコードに直接書かず、環境変数から読み込むことを推奨します。

### macOS / Linux

```bash
export OPENAI_API_KEY="your-openai-api-key"
export OPENWEATHERMAP_API_KEY="your-openweathermap-api-key"
```

### Windows PowerShell

```powershell
$env:OPENAI_API_KEY="your-openai-api-key"
$env:OPENWEATHERMAP_API_KEY="your-openweathermap-api-key"
```

Java側では、次のように読み込みます。

```java
private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
private static final String WEATHER_API_KEY = System.getenv("OPENWEATHERMAP_API_KEY");
```

---

## 実行方法

### 基本サンプルを実行する

```bash
mvn exec:java -Dexec.mainClass="com.example.koog.KoogJavaExample"
```

### マルチツールAIエージェントを実行する

```bash
mvn exec:java -Dexec.mainClass="com.example.koog.MultiToolAgent"
```

---

## `KoogJavaExample.java`

Koogの基本動作を確認するためのサンプルです。

主に次の流れを確認します。

1. `AIAgentBuilder` でエージェントを作成する
2. `SimplePromptExecutorsKt.simpleOpenAIExecutor(apiKey)` でプロンプト実行者を設定する
3. `AIAgent<String, String>` を使用して入力を処理する

このサンプルでは、ジェネリクスを指定した `AIAgent<String, String>` を使用し、OpenAIのプロンプト実行者を設定しています。

---

## `MultiToolAgent.java`

複数のツールを持つAIエージェントのサンプルです。

このクラスでは、次の3つのツールを登録します。

| ツール名 | 内容 |
|---|---|
| `getWeather` | 指定された都市の天気を取得する |
| `translate` | テキストを指定言語に翻訳する |
| `calculate` | 数式を計算する |

---

## 注意点

### APIキーを公開しない

APIキーをソースコードに直接書いた状態でGitHubに公開しないでください。

以下のようなファイルを作成する場合は、必ず `.gitignore` に追加してください。

```text
.env
application.properties
application.yml
```

### KoogのAPIは最新情報を確認する

この記事・サンプルコードは学習用の構成例です。

Koogのパッケージ名、Maven依存関係、APIの書き方は変更される可能性があります。

実際に動かす場合は、公式ドキュメントやMaven Centralで最新情報を確認してください。

### 外部APIのエラーに注意する

天気APIや翻訳APIは、次のような理由で失敗することがあります。

- APIキーが未設定
- APIキーが間違っている
- リクエスト回数制限に達している
- 都市名が正しくない
- ネットワークに接続できない

そのため、実際のアプリケーションではエラー処理を丁寧に書く必要があります。

---

## 今後の拡張案

- Spring Bootと連携する
- データベース検索ツールを追加する
- ファイル読み込みツールを追加する
- 会話履歴を保存する
- Web検索ツールを追加する
- GUIやWeb画面から利用できるようにする

---

## ライセンス

このサンプルは学習目的のコードです。

公開する場合は、必要に応じて `LICENSE` ファイルを追加してください。

