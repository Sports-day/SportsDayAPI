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

| Name               | Description         |
|--------------------|---------------------|
| AZURE_AD_TENANT_ID | Azure AD テナントID     |
| DATABASE_HOST      | データベースホスト           |
| DATABASE_PORT      | データベースポート           |
| DATABASE_USER      | データベースユーザー名         |
| DATABASE_PASSWORD  | データベースパスワード         |
| DATABASE_DB        | データベース名             |
| DISCORD_WEBHOOK    | Discord Webhook URL |
| REDIS_HOST         | Redis ホスト           |
| ALLOWED_HOSTS      | 許可するホスト(CORS)       |
| OUTPUT_REQUEST_LOG | リクエストログを出力するかどうか    |
| OUTPUT_REDIS_LOG   | Redisログを出力するかどうか    |

## 開発

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

## LICENSE

Apache-2.0
Copyright Sports-day
