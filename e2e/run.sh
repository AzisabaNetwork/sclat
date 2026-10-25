#!/usr/bin/env bash
# Sclat シナリオテスト実行スクリプト。
#   1. Sclat.jar をビルド (shadowJar)
#   2. テストサーバー(Spigot 1.14.4 + 依存プラグイン)を組み立て
#   3. ボット2体で試合を1つまるごと実行して検証
#
# 前提: java11 (mise 等)、node/npm、BuildTools でビルド済みの Spigot 1.14.4
set -euo pipefail

cd "$(dirname "$0")/.."
REPO_ROOT=$(pwd)
SCENARIO_DIR="$REPO_ROOT/e2e/scenario"

# Gradle は Java 21 で動かす
JAVA21=${SCLAT_JAVA21:-$(ls -d "$HOME"/.local/share/mise/installs/java/temurin-21* 2>/dev/null | head -1)}
if [ -n "${JAVA21:-}" ]; then
  export JAVA_HOME="$JAVA21"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

# node が PATH に無い場合は mise の最新を使う
if ! command -v node >/dev/null 2>&1; then
  NODE_DIR=$(ls -d "$HOME"/.local/share/mise/installs/node/* 2>/dev/null | sort -V | tail -1)
  if [ -n "${NODE_DIR:-}" ]; then
    export PATH="$NODE_DIR/bin:$PATH"
  fi
fi
if ! command -v node >/dev/null 2>&1; then
  echo "node が見つかりません (mineflayer の実行に必要)" >&2
  exit 1
fi

# Gradle から呼ばれた場合(scenarioTest)は既にビルド済みなので飛ばす
if [ "${SCLAT_SKIP_BUILD:-0}" != "1" ]; then
  echo "==> building Sclat.jar"
  ./gradlew --quiet shadowJar
fi

echo "==> installing bot dependencies (mineflayer)"
cd "$SCENARIO_DIR"
if [ ! -d node_modules ]; then
  npm install --no-audit --no-fund
fi

echo "==> running scenario"
exec node run-match.mjs "$@"
