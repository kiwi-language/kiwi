#!/bin/bash

# ==========================================
# Kiwi Programming Language Installer
# ==========================================

set -e

KIWI_VERSION="0.0.3"
REPO="kiwi-language/kiwi"
INSTALL_DIR="/usr/local/kiwi"
BIN_LINK_DIR="/usr/local/bin"
SERVICE_USER="kiwi"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

printf "${BLUE}Starting Kiwi Installer (v${KIWI_VERSION})...${NC}\n"

# -----------------------------------------------------------------------------
# 1. Detect OS & Arch
# -----------------------------------------------------------------------------
OS="$(uname -s)"
ARCH="$(uname -m)"
ASSET_NAME=""

if [ "$OS" = "Linux" ]; then
    if [ "$ARCH" = "x86_64" ]; then
        ASSET_NAME="kiwi-linux-amd64.zip"
    else
        printf "${RED}Error: Linux architecture $ARCH is not supported yet.${NC}\n"
        exit 1
    fi
elif [ "$OS" = "Darwin" ]; then
    if [ "$ARCH" = "arm64" ]; then
        ASSET_NAME="kiwi-macos-aarch64.zip"
    else
        printf "${RED}Error: macOS architecture $ARCH (Intel) is not supported.${NC}\n"
        exit 1
    fi
else
    printf "${RED}Error: OS $OS is not supported.${NC}\n"
    exit 1
fi

DOWNLOAD_URL="https://github.com/${REPO}/releases/download/${KIWI_VERSION}/${ASSET_NAME}"

# -----------------------------------------------------------------------------
# 2. Check Dependencies
# -----------------------------------------------------------------------------
if ! command -v curl > /dev/null; then printf "${RED}Error: curl required.${NC}\n"; exit 1; fi
if ! command -v unzip > /dev/null; then printf "${RED}Error: unzip required.${NC}\n"; exit 1; fi

# -----------------------------------------------------------------------------
# 3. Download & Extract
# -----------------------------------------------------------------------------
TEMP_DIR=$(mktemp -d)
ZIP_FILE="${TEMP_DIR}/${ASSET_NAME}"
EXTRACT_DIR="${TEMP_DIR}/extract"

printf "Downloading ${BLUE}${ASSET_NAME}${NC}...\n"
curl -L --fail --progress-bar "$DOWNLOAD_URL" -o "$ZIP_FILE"

printf "Extracting files...\n"
mkdir -p "$EXTRACT_DIR"
unzip -q "$ZIP_FILE" -d "$EXTRACT_DIR"

SOURCE_DIR="$EXTRACT_DIR"
if [ $(ls -1 "$EXTRACT_DIR" | wc -l) -eq 1 ]; then
    NESTED_DIR=$(ls -1 "$EXTRACT_DIR")
    if [ -d "$EXTRACT_DIR/$NESTED_DIR" ]; then
        SOURCE_DIR="$EXTRACT_DIR/$NESTED_DIR"
    fi
fi

# -----------------------------------------------------------------------------
# 4. Create Service User
# -----------------------------------------------------------------------------
printf "Ensuring service user '${SERVICE_USER}' exists...\n"

if [ "$OS" = "Darwin" ]; then
    # Check if user exists, if not create using dscl
    if ! id "$SERVICE_USER" &>/dev/null; then
        # Create a user with ID 499 (or find free one), hidden from login screen
        sudo dscl . -create /Users/$SERVICE_USER
        sudo dscl . -create /Users/$SERVICE_USER UserShell /usr/bin/false
        sudo dscl . -create /Users/$SERVICE_USER RealName "Kiwi Language Server"
        sudo dscl . -create /Users/$SERVICE_USER UniqueID "499"
        sudo dscl . -create /Users/$SERVICE_USER PrimaryGroupID "20" # Staff group
        sudo dscl . -create /Users/$SERVICE_USER NFSHomeDirectory /var/empty
        printf "User '${SERVICE_USER}' created.\n"
    fi
elif [ "$OS" = "Linux" ]; then
    if ! id "$SERVICE_USER" &>/dev/null; then
        sudo useradd -r -s /bin/false "$SERVICE_USER"
        printf "User '${SERVICE_USER}' created.\n"
    fi
fi

# -----------------------------------------------------------------------------
# 5. Install Files & Set Permissions
# -----------------------------------------------------------------------------
printf "Installing to ${INSTALL_DIR}...\n"

if [ -d "$INSTALL_DIR" ]; then sudo rm -rf "$INSTALL_DIR"; fi
sudo mkdir -p "$INSTALL_DIR"
sudo cp -R "$SOURCE_DIR/"* "$INSTALL_DIR/"
rm -rf "$TEMP_DIR"

# Change ownership so the service user can read config/write temp files if needed
sudo chown -R "$SERVICE_USER" "$INSTALL_DIR"

# -----------------------------------------------------------------------------
# 6. Link Binaries
# -----------------------------------------------------------------------------
printf "Linking binaries...\n"
link_binary() {
    local BIN_NAME=$1
    local SOURCE="${INSTALL_DIR}/bin/${BIN_NAME}"
    local TARGET="${BIN_LINK_DIR}/${BIN_NAME}"

    if [ ! -f "$SOURCE" ]; then printf "${RED}Error: Binary $SOURCE missing.${NC}\n"; exit 1; fi
    if [ -f "$TARGET" ] || [ -L "$TARGET" ]; then sudo rm "$TARGET"; fi

    sudo ln -s "$SOURCE" "$TARGET"
    sudo chmod +x "$SOURCE"
}
link_binary "kiwi"
link_binary "kiwi-server"

# -----------------------------------------------------------------------------
# 7. Configure Service
# -----------------------------------------------------------------------------
printf "Configuring ${BLUE}kiwi-server${NC} service as user '${SERVICE_USER}'...\n"

if [ "$OS" = "Darwin" ]; then
    PLIST_PATH="/Library/LaunchDaemons/com.kiwi.server.plist"
    LOG_OUT="/var/log/kiwi-server.log"

    # Ensure log files exist and are writable by kiwi user
    sudo touch "$LOG_OUT"
    sudo chown "$SERVICE_USER" "$LOG_OUT"

    sudo bash -c "cat > $PLIST_PATH" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.kiwi.server</string>
    <key>UserName</key>
    <string>${SERVICE_USER}</string>
    <key>ProgramArguments</key>
    <array>
        <string>${INSTALL_DIR}/bin/kiwi-server</string>
    </array>
    <key>WorkingDirectory</key>
    <string>${INSTALL_DIR}</string>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
    <key>StandardErrorPath</key>
    <string>${LOG_OUT}</string>
    <key>StandardOutPath</key>
    <string>${LOG_OUT}</string>
</dict>
</plist>
EOF
    sudo chown root:wheel "$PLIST_PATH"
    sudo chmod 644 "$PLIST_PATH"
    sudo launchctl unload "$PLIST_PATH" 2>/dev/null || true
    sudo launchctl load "$PLIST_PATH"

elif [ "$OS" = "Linux" ]; then
    if command -v systemctl > /dev/null; then
        SERVICE_PATH="/etc/systemd/system/kiwi-server.service"
        sudo bash -c "cat > $SERVICE_PATH" <<EOF
[Unit]
Description=Kiwi Language Server
After=network.target

[Service]
Type=simple
User=${SERVICE_USER}
Group=${SERVICE_USER}
ExecStart=${INSTALL_DIR}/bin/kiwi-server
WorkingDirectory=${INSTALL_DIR}
Restart=always

[Install]
WantedBy=multi-user.target
EOF
        sudo systemctl daemon-reload
        sudo systemctl enable kiwi-server
        sudo systemctl restart kiwi-server
    fi
fi

# -----------------------------------------------------------------------------
# 8. Verify
# -----------------------------------------------------------------------------
printf "${GREEN}Kiwi installed successfully!${NC}\n"
printf "Service running as user: ${SERVICE_USER}\n"