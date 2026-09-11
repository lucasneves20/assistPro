#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

# Publica uma nova versao assinada como GitHub Release.
# Uso: tools/release.sh <versao> [notas...]
# Ex.: tools/release.sh 1.1 "Corrige leitura de codigo de barras"
#
# Requer o gh autenticado (gh auth login) e o keystore.properties presente.

VERSAO="${1:?informe a versao, ex.: 1.1}"
shift || true
NOTAS="${*:-Release ${VERSAO}}"
TAG="v${VERSAO}"

cd "$(dirname "$0")/.."

echo "==> Compilando release ${VERSAO}..."
gradle :app:assembleRelease "-Passistpro.versionName=${VERSAO}"

APK="app/build/outputs/apk/release/app-release.apk"
[ -f "$APK" ] || { echo "APK nao encontrado: $APK"; exit 1; }

echo "==> Publicando ${TAG}..."
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
gh release create "$TAG" "$APK" --title "assistPro ${VERSAO}" --notes "${NOTAS}" --target "$BRANCH"

REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner)"
echo "==> Pronto: https://github.com/${REPO}/releases/tag/${TAG}"
