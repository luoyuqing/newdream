# NewDream APK 签名配置说明

## 现状

- **keystore**: `keystore/newdream-release.p12` (PKCS#12 格式)
- **密码**: `NewDream@2026`
- **别名**: `newdream`
- **证书DN**: `CN=luoyuqing, OU=NewDream, O=Personal, L=Chengdu, ST=Sichuan, C=CN`
- **有效期**: 2026-07-28 ~ 2056-07-28 (30年)
- **密钥长度**: RSA 2048-bit
- **SHA-256 指纹**: `b582719153946ac59c2b09ed732de238e1d9b356bfdd7edb0be7c14b663f5031`

## 本地构建

`keystore.properties` 已经在仓库根目录，包含上述密码。Gradle 会自动读取并签名 release APK。

```bash
./gradlew assembleRelease
# 产物: app/build/outputs/apk/release/app-release.apk (已签名)
```

## CI 构建 (GitHub Actions)

CI 工作流从 GitHub Secrets 读取密钥。需要配置以下 3 个 secret:

| Secret 名称 | 值 | 来源 |
|------------|------|------|
| `NEWDREAM_KEYSTORE_BASE64` | `keystore/newdream-release.p12.b64` 的全部内容 | base64 编码后的 p12 |
| `NEWDREAM_STORE_PASSWORD` | `NewDream@2026` | keystore 密码 |
| `NEWDREAM_KEY_PASSWORD` | `NewDream@2026` | 私钥密码 |

### 配置步骤

1. 打开 https://github.com/luoyuqing/newdream/settings/secrets/actions
2. 点击 "New repository secret"
3. 添加以上 3 个 secret

工作流会自动：
- 解码 `NEWDREAM_KEYSTORE_BASE64` 为 `app/keystore/newdream-release.p12`
- 生成 `keystore.properties`
- `assembleRelease` 会读取这些配置，签出已签名的 release APK

## 备份

`keystore/newdream-release.p12` 和 `keystore/newdream-release.p12.b64` 都已备份在本地 D:\workbuddy\newdream\keystore\ 目录。

⚠️ **重要**：
- `keystore.properties` 已加入 .gitignore，不会被提交
- 真实的 keystore 文件也已被 .gitignore 排除
- 必须保管好本地 keystore 文件，丢失后无法再签出可覆盖安装的 APK（密钥不同必须卸载重装）
