# assistPro

App Android (Kotlin) para guardar e acompanhar boletos brasileiros.

## Recursos

- Le a linha digitavel por OCR (camera, galeria ou arquivo PDF/imagem).
- Preenche valor e vencimento automaticamente a partir do codigo.
- Organiza os boletos por mes, com total em aberto.
- Marca como pago e exclui.
- Notifica o vencimento um dia antes (as 09:00).
- **Auto-update**: verifica os releases deste repositorio e se atualiza sozinho.

## Como atualizar o app

O app consulta `releases/latest` no GitHub e compara a tag com a versao instalada.
Para publicar uma nova versao assinada:

```bash
tools/release.sh 1.1 "notas da versao"
```

Isso compila o APK de release assinado (`-Passistpro.versionName=1.1`), cria a tag
`v1.1` e envia o APK como asset do release. Os apps instalados passam a oferecer a
atualizacao.

> O pacote de release precisa estar assinado sempre com o mesmo `release.keystore`,
> senao o Android recusa a instalacao por cima. Guarde `release.keystore` e
> `keystore.properties` (ambos fora do git) com seguranca.

## Build

Requer Android SDK e Gradle. No Termux, o `gradle.properties` define
`android.aapt2FromMavenOverride` e `local.properties` aponta o `sdk.dir`.

```bash
gradle :app:assembleDebug     # APK de debug
gradle :app:assembleRelease   # APK de release assinado (usa keystore.properties)
```

## Configuracao

- `assistpro.updateRepo` em `gradle.properties`: repositorio `owner/repo` usado pelo auto-update.
- `keystore.properties` (nao versionado): caminho/senha do keystore de release.
