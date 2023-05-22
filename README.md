# SportsDayAPI

Sports-dayのバックエンド用レポジトリです。

## Environment Variables

| Name               | Description         | Default |
|--------------------|---------------------|---------|
| AZURE_AD_TENANT_ID | Azure AD テナントID     |         |
| DATABASE_HOST      | データベースホスト           |         |
| DATABASE_PORT      | データベースポート           |         |
| DATABASE_USER      | データベースユーザー名         |         |
| DATABASE_PASSWORD  | データベースパスワード         |         |
| DATABASE_DB        | データベース名             |         |
| DISCORD_WEBHOOK    | Discord Webhook URL |         |
| REDIS_HOST         | Redis ホスト           |         |
| ALLOWED_HOSTS      | 許可するホスト(CORS)       |         |
| OUTPUT_REQUEST_LOG | リクエストログを出力するかどうか |    |
| OUTPUT_REDIS_LOG | Redisログを出力するかどうか |    |

## Develop

### Git branch

``main``: プロダクション用ブランチ

開発時は、``main``ブランチからブランチを切ってください。

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