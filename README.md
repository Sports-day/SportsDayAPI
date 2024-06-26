![api_banner](https://github.com/Sports-day/SportsDayAPI/assets/58895178/b3aa3af8-c332-4960-a31d-c707a85b910b)

# SportsDayAPI

SportsDayAPIは、各リソースへのアクセス、認証を行うREST APIです。

- Ktorによる高速化 🚀
- キャッシュシステムでDB負荷を軽減 🎈
- Kubernetesサポート 💪

## 動作環境

- JDK 11
- MySQL 8.0
- Redis 7.0 (クラスター化するに必須)

## 環境変数

| Name                   | Description             |
|------------------------|-------------------------|
| JWT_SECRET             | JWTシークレット(32文字のランダム文字列) |
| JWT_ISSUER             | JWT発行者名                 |
| JWT_EXPIRE_MINUTE      | JWT有効期限(分)              |
| JWT_REALMS             | JWTリアルム                 |
| COOKIE_SECURE          | クッキーセキュアフラグ             |
| OIDC_CLIENT_ID         | OIDCクライアントID            |
| OIDC_CLIENT_SECRET     | OIDCクライアントシークレット        |
| OIDC_TOKEN_ENDPOINT    | OIDCトークンエンドポイント         |
| OIDC_JWKS_ENDPOINT     | OIDC JWKSエンドポイント        |
| OIDC_USERINFO_ENDPOINT | OIDCユーザー情報エンドポイント       |
| OIDC_ISSUER            | OIDC発行者名                |
| OIDC_REDIRECT_URI      | OIDCリダイレクトURI           |
| OIDC_SCOPE             | OIDCスコープ                |
| DATABASE_HOST          | データベースホスト               |
| DATABASE_PORT          | データベースポート               |
| DATABASE_USER          | データベースユーザー名             |
| DATABASE_PASSWORD      | データベースパスワード             |
| DATABASE_DB            | データベース名                 |
| DISCORD_WEBHOOK        | Discord Webhook URL     |
| REDIS_HOST             | Redis ホスト               |
| ALLOWED_HOSTS          | 許可するホスト(CORS)           |
| OUTPUT_REQUEST_LOG     | リクエストログを出力するかどうか        |
| OUTPUT_REDIS_LOG       | Redisログを出力するかどうか        |

## 開発

### Linter

コミットする前にlinter formatを実行すると幸せになります。

```shell
./gradlew ktlintFormat
```

### Git branch

``main``ブランチから作業用のブランチを切って作業を行います。

#### Style

```
<type>/#<issue-number>-<alias>
```

#### Type

``main``: プロダクション用ブランチ  
``feature``: 開発用ブランチ

### Git commit

#### Template

```
<type>: <subject>
```

#### Type

- **feat**: 新機能
- **change**: 修正・削除
- **fix**: バグフィックス
- **docs**: ドキュメントに関する変更
- **style**: フォーマット等の変更
- **refactor**: リファクタに関する変更
- **debug**: デバック用のコード
- **test**: テストコードの追加・更新
- **chore**: GitHub Actions等タスクに関する変更

## LICENSE

Apache-2.0
Copyright Sports-day
