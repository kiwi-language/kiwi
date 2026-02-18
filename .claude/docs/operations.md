# Manul Operations & Deployment

## This Machine's Development Environment

**Manul Installation**:
- Installed at: `~/.manul`
- CLI: `~/.manul/bin/manul`, Server: `~/.manul/bin/manul-server`
- Environment file: `~/.manul/bin/env`

**Manul Server Service**:
- LaunchAgent: `~/Library/LaunchAgents/com.manul.server.plist`
- Working directory: `/Users/leen/.manul`
- Auto-start: `RunAtLoad=true`, `KeepAlive=true`
- Logs: `/Users/leen/Library/Logs/Manul/manul-server.log`

```bash
# Service management
launchctl load ~/Library/LaunchAgents/com.manul.server.plist
launchctl unload ~/Library/LaunchAgents/com.manul.server.plist
launchctl start com.manul.server
launchctl stop com.manul.server
launchctl list | grep manul
tail -f ~/Library/Logs/Manul/manul-server.log
```

**PostgreSQL**:
- Port: 5432, Host: 127.0.0.1, Database: `kiwi` (not `manul`!)
- Username: `postgres`, Password: `85263670`
- Version: PostgreSQL 18 (Homebrew), Path: `/opt/homebrew/opt/postgresql@18/bin/postgres`

**Elasticsearch/OpenSearch**:
- Port: 9200, Host: localhost
- User: `elastic`, Password: `DjgoqCxwI9SOXqLCvotv`
- Installation: `~/develop/elasticsearch`
- Server config uses `opensearch` key (compatible with ES API)

**Local Server**: Port 8080, mode: `persistent`

**Active Development**:
- IntelliJ IDEA debug instance, Config: `/etc/manul/manul.yml`
- Java: `/Users/leen/.sdkman/candidates/java/21.0.2-graalce/bin/java`

**Common Operations**:
```bash
~/.manul/bin/manul --version
psql -h 127.0.0.1 -p 5432 -U postgres -d kiwi
lsof -i :5432
curl http://localhost:9200/_cluster/health
curl -X POST http://localhost:8080/manul-system/bootstrap/rebuild-index/{appId}
curl -X POST http://localhost:8080/manul-system/bootstrap/reindex/{appId}
```

---

## Local Installation

```bash
# Build and install
mvn clean install -DskipTests -pl dist -am
./install.sh
```

**install.sh**: Stops service → removes `~/.manul` → unpacks `dist/target/manul.zip` → copies config from `/etc/manul/manul.yml` → starts service → verifies port 8080.

**Manual installation**:
```bash
launchctl stop com.manul.server
launchctl unload ~/Library/LaunchAgents/com.manul.server.plist
rm -rf ~/.manul
unzip -q -d ~ dist/target/manul.zip
mv ~/manul ~/.manul
cp -f /etc/manul/manul.yml ~/.manul/conf
launchctl load ~/Library/LaunchAgents/com.manul.server.plist
```

**Important**: `mvn clean install` does NOT update the running server. Must build dist, stop, replace, restart.

---

## Production Deployment

**Architecture**:
- Merge to `main` does NOT auto-deploy
- Deployment triggered by recreating `0.0.1-alpha` release tag
- GitHub Actions builds native images (Mac, Linux, Windows, Alpine)
- Artifacts uploaded to GitHub Releases and Aliyun OSS

**Git Workflow**:
- Feature branches rebased against `origin/main`
- All commits squashed into single commit per PR
- Linear history on main branch

### Automated Scripts

**Full MR + Deploy** (`./mr-and-deploy.sh`):
```bash
./mr-and-deploy.sh feat/new-feature          # Full workflow
./mr-and-deploy.sh fix/bug-123 --skip-tests   # Skip local tests
./mr-and-deploy.sh refactor/cleanup --skip-deploy  # PR only
```

Steps: local tests → create branch → rebase → squash → push → create PR → wait CI → merge → recreate release tag.

Flags: `--skip-tests`, `--skip-pr`, `--skip-deploy`

**Deploy Only** (`./deploy.sh`):
```bash
./deploy.sh  # Must be on main with clean working directory
```

Steps: verify main + clean → pull latest → delete `0.0.1-alpha` tag → recreate at HEAD → push.

### Manual Process

```bash
# 1. Create and merge PR
git checkout -b feat/my-feature
git add . && git commit -m "feat: description"
git fetch origin main && git rebase origin/main
git push -u origin feat/my-feature --force-with-lease
gh pr create --base main --head feat/my-feature --fill
gh pr merge feat/my-feature --squash --delete-branch

# 2. Deploy
git checkout main && git pull origin main
git tag -d 0.0.1-alpha && git push origin :refs/tags/0.0.1-alpha
git tag -a 0.0.1-alpha -m "Release 0.0.1-alpha - $(date +%Y-%m-%d)"
git push origin 0.0.1-alpha
```

### GitHub Actions Workflows

**ci.yml**: PR/push to main → `mvn -B verify` → Temurin JDK 21

**release-asset-upload.yml**: Release/tag → native images (macOS aarch64/amd64, Windows amd64, Linux amd64/aarch64, Alpine amd64/aarch64) → GraalVM native-image → upload to GitHub Releases + Aliyun OSS. Build time: ~4 minutes.

### Deployment Checklist

Before: tests pass, code reviewed, changes documented, migrations prepared.
After: verify build (~4 min), check GitHub Release, verify OSS artifacts, test download.

---

## Elasticsearch Recovery & Deployment

### Two Different Reindex Operations

1. **Database Index Rebuild** - `POST /manul-system/bootstrap/reindex/{appId}`
   - Calls `ApplicationManager.reindex()` → `ReindexTask` → `context.forceReindex()`
   - Rebuilds **PostgreSQL database indexes** only, does NOT touch ES

2. **Elasticsearch Index Rebuild** - `POST /manul-system/bootstrap/rebuild-index/{appId}`
   - Calls `TaskManager.addIndexRebuildTask()` → `IndexRebuildTask` → `SearchSync.sync()`
   - Rebuilds **Elasticsearch indices** from PostgreSQL data

### ES Recovery Process

```bash
# 1. Verify data in PostgreSQL
psql -h 127.0.0.1 -p 5432 -U postgres -d kiwi \
  -c "SELECT COUNT(*) FROM instance_{appId} WHERE deleted_at = 0;"

# 2. Trigger rebuild
curl -X POST http://localhost:8080/manul-system/bootstrap/rebuild-index/{appId}

# 3. Monitor
tail -f ~/Library/Logs/Manul/manul-server.log | grep IndexRebuildTask

# 4. Verify
curl -u elastic:PASSWORD http://localhost:9200/instance-main-{appId}/_count
```

**Global rebuild** (use sparingly): `curl -X POST http://localhost:8080/manul-system/bootstrap/rebuild-index`

### ES Single-Node Configuration

Single-node needs `number_of_replicas: 0`:
```bash
curl -X PUT -u elastic:PASSWORD http://localhost:9200/_all/_settings \
  -H 'Content-Type: application/json' -d '{"index": {"number_of_replicas": 0}}'
```

**Index naming**: Versioned `instance-{appId}-v1`, alias `instance-main-{appId}` → versioned index. Never create index with same name as alias.

### Emergency ES Cleanup

```bash
curl -u elastic:PASSWORD http://localhost:9200/_cat/indices?h=index
curl -X DELETE -u elastic:PASSWORD http://localhost:9200/{index_name}
```

### ES Recovery Case Study (Feb 2026)

Problem: Accidentally cleared ES data for app 1000061024. Resolution: data intact in PostgreSQL (33,668 docs), added new rebuild-index endpoint, cleaned 200+ unnecessary indices, recovered 33,646 docs. Lesson: always use targeted single-app rebuild, never global.

---

## Aliyun OSS Infrastructure

**Purpose**: Fast global distribution of release binaries.

- Bucket: `manul-packages`, Region: `oss-cn-hongkong`
- Domain: `pkg.metavm.tech`, Access: public read

**URLs**:
```
https://pkg.metavm.tech/releases/0.0.1-alpha/manul-{platform}.tar.gz
https://pkg.metavm.tech/releases/latest/manul-{platform}.tar.gz
```

**GitHub Secrets**: `ALIYUN_ACCESS_KEY_ID`, `ALIYUN_ACCESS_KEY_SECRET`

**Performance**: OSS upload ~2 min (vs Gitee ~30+ min, 15x faster).

**Setup**: `./setup-aliyun-oss.py` — creates bucket, configures DNS, sets CORS, creates directories.
