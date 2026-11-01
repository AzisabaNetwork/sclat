# ビルドを通すまで

`./gradlew build` が通るまでの手順だけを書きます。所要時間は初回 15〜30分(Spigot のビルド込み)、2回目以降は10秒程度です。

## 前提

| 必要なもの | バージョン | 補足 |
| --- | --- | --- |
| Gradle を動かす JDK | **21**(17 でも可) | **25 では動きません**(後述)。`JAVA_HOME` をこれに向ける |
| コンパイル用 JDK | 11 | Gradle が自動ダウンロードするので用意しなくてよい |
| Spigot 1.14.4 | `1.14.4-R0.1-SNAPSHOT` | 自分でビルドして `~/.m2` に入れる必要あり(手順2) |

この後のコマンドは macOS/Linux と Windows(PowerShell)の両方を書きます。**Windows の人は PowerShell の行だけ実行**してください。

## 手順1: Gradle を JDK 21 で動かす

```bash
# macOS / Linux
java -version                     # 25 が出るなら要切り替え
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS の例
```

```powershell
# Windows (PowerShell)
java -version                     # 25 が出るなら要切り替え
winget install EclipseAdoptium.Temurin.21.JDK     # 未インストールなら
(Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Filter '*21*').FullName   # パスを確認
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot'      # 上で出たパス
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version                     # 21 になったことを確認
```

`$env:JAVA_HOME` はその PowerShell を閉じると消えます。毎回設定したくない場合は「システム環境変数」に `JAVA_HOME` を追加してください。

`build.gradle.kts` は `jvmToolchain(11)` なので、コンパイルには JDK 11 が使われます。無ければ Gradle が自動で取得します(`%USERPROFILE%\.gradle\jdks`)。Windows でも同じです。

## 手順2: Spigot 1.14.4 を `~/.m2` に入れる

Spigot は再配布されていないため、BuildTools でローカルビルドします(**初回のみ・10〜20分**)。

### macOS / Linux

```bash
mkdir -p ~/buildtools && cd ~/buildtools
curl -LO "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
java -jar BuildTools.jar --rev 1.14.4 --nogui
```

### Windows (PowerShell)

```powershell
mkdir $HOME\buildtools -Force; cd $HOME\buildtools
curl.exe -LO "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
java -jar BuildTools.jar --rev 1.14.4 --nogui
```

Windows では **Git for Windows がインストール済み(git コマンドが通る)であること**が必要です。BuildTools は内部で git を使います。

> **BuildTools が git の author 未設定で止まったら**
> `git config user.name` / `user.email` が未設定だと失敗します。グローバル設定を変えたくない場合は、その場だけ環境変数で渡してください。
>
> ```bash
> # macOS / Linux(コマンドの前に付ける)
> GIT_AUTHOR_NAME=dev GIT_AUTHOR_EMAIL=dev@example.com \
> GIT_COMMITTER_NAME=dev GIT_COMMITTER_EMAIL=dev@example.com \
> java -jar BuildTools.jar --rev 1.14.4 --nogui
> ```
>
> ```powershell
> # Windows (PowerShell)
> $env:GIT_AUTHOR_NAME='dev';    $env:GIT_AUTHOR_EMAIL='dev@example.com'
> $env:GIT_COMMITTER_NAME='dev'; $env:GIT_COMMITTER_EMAIL='dev@example.com'
> java -jar BuildTools.jar --rev 1.14.4 --nogui
> ```
>
> 設定済みかどうかは `git config --global user.name` / `git config --global user.email` で確認できます。

次のファイルができていれば OK です。

```text
macOS/Linux: ~/.m2/repository/org/spigotmc/spigot/1.14.4-R0.1-SNAPSHOT/spigot-1.14.4-R0.1-SNAPSHOT.jar
Windows:     C:\Users\<ユーザー名>\.m2\repository\org\spigotmc\spigot\1.14.4-R0.1-SNAPSHOT\spigot-1.14.4-R0.1-SNAPSHOT.jar
```

`build.gradle.kts` は `mavenLocal()` を `org.spigotmc` だけに限定して参照しているので、ここに置くだけで解決されます。

## 手順3: ビルド

```bash
# macOS / Linux
cd /path/to/sclat
./gradlew build
```

```powershell
# Windows (PowerShell)
cd C:\path\to\sclat
.\gradlew.bat build
```

`BUILD SUCCESSFUL` が出れば成功です。成果物:

```text
build/libs/Sclat.jar              # サーバーに入れる jar(依存を同梱・relocate 済み)
build/libs/Sclat-1.0-SNAPSHOT.jar # 依存を含まない素の jar
```

`build` はコンパイル + ktlint(コード整形チェック)+ `shadowJar` をまとめて実行します。

## うまくいかないとき

| 症状 | 原因と対処 |
| --- | --- |
| `* What went wrong: 25.0.3` で即失敗 | Gradle を JDK 25 で起動している。手順1で `JAVA_HOME` を 21 に向ける |
| `Could not find org.spigotmc:spigot:1.14.4-R0.1-SNAPSHOT` | 手順2の Spigot が `~/.m2` に無い。BuildTools を実行する |
| ktlint のエラーで `build` が止まる | `./gradlew ktlintFormat`(Windows は `.\gradlew.bat ktlintFormat`)で自動修正して再実行 |
| 依存が取れない(JitPack など) | ネットワーク/プロキシを確認し、`build --refresh-dependencies` を付けて再実行 |
| Windows で `./gradlew` が動かない | PowerShell では `.\gradlew.bat build` を使う(`./gradlew` は Git Bash / WSL 用) |
| Windows で BuildTools が失敗する | Git for Windows を入れる。git の author 未設定なら上の Tips を参照。パスに日本語や空白がある場合は `C:\buildtools` のような短いパスで実行 |
| Windows で `curl` が無い | Windows 10 以降は `curl.exe` が標準搭載。古い場合は `Invoke-WebRequest -Uri <URL> -OutFile BuildTools.jar` |
