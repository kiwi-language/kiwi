# Aliyun OSS Setup for Manul Package Distribution

## Overview

This document explains the Aliyun OSS infrastructure setup for distributing Manul release packages.

## Infrastructure

### OSS Bucket
- **Bucket Name**: `manul-packages`
- **Region**: `oss-cn-hongkong` (Hong Kong for better international access)
- **Endpoint**: `https://oss-cn-hongkong.aliyuncs.com`
- **Custom Domain**: `pkg.metavm.tech`
- **Access**: Public read

### DNS Configuration
- **Domain**: `pkg.metavm.tech`
- **Type**: CNAME
- **Target**: `manul-packages.oss-cn-hongkong.aliyuncs.com`
- **Status**: ✅ Configured via Aliyun DNS

### Directory Structure
```
manul-packages/
├── releases/
│   ├── 0.0.1-alpha/
│   │   ├── manul-macos-aarch64.tar.gz
│   │   ├── manul-macos-amd64.tar.gz
│   │   ├── manul-linux-amd64.tar.gz
│   │   ├── manul-linux-aarch64.tar.gz
│   │   ├── manul-alpine-amd64.tar.gz
│   │   ├── manul-alpine-aarch64.tar.gz
│   │   └── manul-windows-amd64.zip
│   └── latest/
│       └── (same files as latest release)
```

## GitHub Secrets Configuration

Add the following secrets to your GitHub repository:

**Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Value |
|-------------|-------|
| `ALIYUN_ACCESS_KEY_ID` | `<your-access-key-id>` |
| `ALIYUN_ACCESS_KEY_SECRET` | `<your-access-key-secret>` |

> **Note**: Get these values from `/Users/leen/develop/env/aliyun.properties` (local only, not in git)

**Note**: These are already configured in the workflow file:
- `ALIYUN_OSS_ENDPOINT`: `https://oss-cn-hongkong.aliyuncs.com`
- `ALIYUN_OSS_BUCKET`: `manul-packages`

## Manual Configuration Required

### 1. Enable Transfer Acceleration

Transfer acceleration could not be enabled via API. Enable it manually:

1. Go to [Aliyun OSS Console](https://oss.console.aliyun.com/)
2. Select bucket: `manul-packages`
3. Navigate to **Transmission Management** → **Transfer Acceleration**
4. Enable **Global Acceleration**

Once enabled, the accelerated endpoint will be:
- `https://manul-packages.oss-accelerate.aliyuncs.com`

### 2. Bind Custom Domain

Domain binding had API issues. Verify it's configured:

1. Go to [Aliyun OSS Console](https://oss.console.aliyun.com/)
2. Select bucket: `manul-packages`
3. Navigate to **Domain Management**
4. Verify `pkg.metavm.tech` is bound
5. If not, click **Bind Custom Domain** and add `pkg.metavm.tech`

## Download URLs

### Versioned Release
```
https://pkg.metavm.tech/releases/0.0.1-alpha/manul-macos-aarch64.tar.gz
https://pkg.metavm.tech/releases/0.0.1-alpha/manul-linux-amd64.tar.gz
https://pkg.metavm.tech/releases/0.0.1-alpha/manul-windows-amd64.zip
```

### Latest Release
```
https://pkg.metavm.tech/releases/latest/manul-macos-aarch64.tar.gz
https://pkg.metavm.tech/releases/latest/manul-linux-amd64.tar.gz
https://pkg.metavm.tech/releases/latest/manul-windows-amd64.zip
```

## GitHub Actions Workflow

The updated workflow (`.github/workflows/release-asset-upload.yml`) now:

1. **Builds** native images for all platforms (Mac, Windows, Linux, Alpine)
2. **Uploads to GitHub Releases** (for GitHub users)
3. **Uploads to Aliyun OSS** (for faster international downloads)

### Previous vs New

**Before** (slow):
- Upload to GitHub Releases: ~2 min ✅
- Upload to Gitee: ~30+ min ❌ (very slow from GitHub Actions)
- **Total**: ~32+ minutes

**After** (fast):
- Upload to GitHub Releases: ~2 min ✅
- Upload to Aliyun OSS: ~2 min ✅ (fast from GitHub Actions)
- **Total**: ~4 minutes 🚀

**Speed improvement**: ~8x faster!

## Testing

Test the setup by downloading a file:

```bash
# Test download
curl -I https://pkg.metavm.tech/releases/0.0.1-alpha/

# Once release is deployed, test actual download
curl -L -o manul.tar.gz \
  https://pkg.metavm.tech/releases/latest/manul-linux-amd64.tar.gz
```

## Installation Script Update

Update the installation script to use OSS URLs:

```bash
# Current (GitHub)
DOWNLOAD_URL="https://github.com/wizardleeen/manul/releases/download/0.0.1-alpha/manul-${OS}-${ARCH}.${EXT}"

# New (Aliyun OSS - faster)
DOWNLOAD_URL="https://pkg.metavm.tech/releases/latest/manul-${OS}-${ARCH}.${EXT}"
```

## Maintenance

### Cleanup Old Releases

```bash
# List all releases
ossutil64 ls oss://manul-packages/releases/

# Delete old release
ossutil64 rm oss://manul-packages/releases/0.0.0-old/ -r -f
```

### Monitor Storage

```bash
# Check bucket storage usage
ossutil64 du oss://manul-packages/
```

## Security

- **Access Keys**: Stored in GitHub Secrets (encrypted)
- **Bucket ACL**: Public read (required for downloads), private write
- **CORS**: Configured for web access (GET, HEAD methods only)

## Troubleshooting

### Upload Fails in GitHub Actions

Check the GitHub Actions logs for:
- Authentication errors → Verify secrets are set correctly
- Network errors → Check OSS endpoint accessibility
- Permission errors → Verify access key has write permissions

### Domain Not Working

1. Verify DNS: `dig pkg.metavm.tech`
   - Should return CNAME: `manul-packages.oss-cn-hongkong.aliyuncs.com`
2. Verify domain binding in OSS console
3. Wait for DNS propagation (up to 24 hours)

### Slow Downloads

- Enable transfer acceleration (see manual steps above)
- Consider using CDN (Aliyun CDN) for even faster global distribution

## Cost Estimation

Approximate costs (Hong Kong region):
- **Storage**: $0.025/GB/month
- **Outbound traffic**: $0.12/GB (first 10TB)
- **Requests**: $0.01/10,000 requests

For ~50MB packages with 1000 downloads/month:
- Storage: ~50 packages × 50MB × 7 platforms = 17.5GB → $0.44/month
- Traffic: 1000 × 50MB × 7 = 350GB → $42/month
- Requests: 1000 × 7 = 7,000 → $0.007/month

**Total**: ~$43/month (high estimate, actual will be lower)

**Compare to**:
- GitHub Bandwidth: Unlimited for open source ✅
- Gitee Bandwidth: Unlimited but very slow from international ❌

## Next Steps

1. ✅ OSS bucket created and configured
2. ✅ DNS configured for `pkg.metavm.tech`
3. ✅ GitHub Actions workflow updated
4. ⏳ Add GitHub Secrets (manual step required)
5. ⏳ Enable transfer acceleration (manual step required)
6. ⏳ Test by creating a new release
7. ⏳ Update installation script to use OSS URLs
