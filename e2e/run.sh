#!/usr/bin/env bash
# Sclat シナリオテスト実行スクリプト。
#   1. Sclat.jar をビルド (shadowJar)
#   2. テストサーバー(Spigot 1.14.4 + 依存プラグイン)を組み立て
#   3. ボット2体で試合を1つまるごと実行して検証
#
# 引数はそのまま run-match.mjs に渡る:
#   bash e2e/run.sh --work-mode TDM
#   bash e2e/run.sh --work-mode Area --verbose
#
# 前提: java11 (mise 等)、node + pnpm、BuildTools でビルド済みの Spigot 1.14.4
set -euo pipefail

cd "$(dirname "$0")/.."
REPO_ROOT=$(pwd)
SCENARIO_DIR="$REPO_ROOT/e2e/scenario"

# Gradle は Java 21 で動かす (CI などで JAVA_HOME が既に適切なら触らない)
if [ -z "${JAVA_HOME:-}" ]; then
  JAVA21=${SCLAT_JAVA21:-$(ls -d "$HOME"/.local/share/mise/installs/java/temurin-21* 2>/dev/null | head -1)}
  if [ -n "${JAVA21:-}" ]; then
    export JAVA_HOME="$JAVA21"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

# node / pnpm が PATH に無い場合は mise の最新を使う
if ! command -v node >/dev/null 2>&1; then
  NODE_DIR=$(ls -d "$HOME"/.local/share/mise/installs/node/* 2>/dev/null | sort -V | tail -1)
  if [ -n "${NODE_DIR:-}" ]; then
    export PATH="$NODE_DIR/bin:$PATH"
  fi
fi
if ! command -v pnpm >/dev/null 2>&1; then
  PNPM_DIR=$(ls -d "$HOME"/.local/share/mise/installs/pnpm/* 2>/dev/null | sort -V | tail -1)
  if [ -n "${PNPM_DIR:-}" ]; then
    export PATH="$PNPM_DIR:$PATH"
  fi
fi
for cmd in node pnpm; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "$cmd が見つかりません (mineflayer の実行に必要)" >&2
    exit 1
  fi
done

# Gradle から呼ばれた場合(scenarioTest)は既にビルド済みなので飛ばす
if [ "${SCLAT_SKIP_BUILD:-0}" != "1" ]; then
  echo "==> building Sclat.jar"
  ./gradlew --quiet shadowJar
fi

echo "==> installing bot dependencies (mineflayer)"
cd "$SCENARIO_DIR"
if [ ! -d node_modules ]; then
  if [ -f pnpm-lock.yaml ]; then
    pnpm install --frozen-lockfile
  else
    pnpm install
  fi
fi

echo "==> running scenario"
exec node run-match.mjs "$@"
