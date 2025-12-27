#!/bin/bash

# ==========================================
# Kiwi Programming Language Installer
# ==========================================

set -e

KIWI_VERSION="0.0.3"
REPO="kiwi-language/kiwi"
INSTALL_DIR="/usr/local/kiwi"
BIN_LINK_DIR="/usr/local/bin"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

printf "${BLUE}Starting Kiwi Installer (v${KIWI_VERSION})...${NC}\n"

# -----------------------------------------------------------------------------
# 1. Detect Operating System and Architecture
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
        printf "${RED}Error: macOS architecture $ARCH (Intel) is not supported in this release.${NC}\n"
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
# 3. Download
# -----------------------------------------------------------------------------
TEMP_DIR=$(mktemp -d)
ZIP_FILE="${TEMP_DIR}/${ASSET_NAME}"
EXTRACT_DIR="${TEMP_DIR}/extract"

printf "Downloading ${BLUE}${ASSET_NAME}${NC}...\n"
curl -L --fail --progress-bar "$DOWNLOAD_URL" -o "$ZIP_FILE"

printf "Extracting files...\n"
mkdir -p "$EXTRACT_DIR"
unzip -q "$ZIP_FILE" -d "$EXTRACT_DIR"

# -----------------------------------------------------------------------------
# 4. Handle Zip Structure (Flattening)
# -----------------------------------------------------------------------------
SOURCE_DIR="$EXTRACT_DIR"
if [ $(ls -1 "$EXTRACT_DIR" | wc -l) -eq 1 ]; then
    NESTED_DIR=$(ls -1 "$EXTRACT_DIR")
    if [ -d "$EXTRACT_DIR/$NESTED_DIR" ]; then
        SOURCE_DIR="$EXTRACT_DIR/$NESTED_DIR"
    fi
fi

# -----------------------------------------------------------------------------
# 5. Install Files
# -----------------------------------------------------------------------------
printf "Installing to ${INSTALL_DIR}...\n"

if [ -d "$INSTALL_DIR" ]; then sudo rm -rf "$INSTALL_DIR"; fi
sudo mkdir -p "$INSTALL_DIR"
sudo cp -R "$SOURCE_DIR/"* "$INSTALL_DIR/"
rm -rf "$TEMP_DIR"

# -----------------------------------------------------------------------------
# 6. Link Binaries
# -----------------------------------------------------------------------------
printf "Linking binaries...\n"

link_binary() {
    local BIN_NAME=$1
    local SOURCE="${INSTALL_DIR}/bin/${BIN_NAME}"
    local TARGET="${BIN_LINK_DIR}/${BIN_NAME}"

    if [ ! -f "$SOURCE" ]; then
        printf "${RED}Error: Binary $SOURCE missing.${NC}\n"; exit 1
    fi

    if [ -f "$TARGET" ] || [ -L "$TARGET" ]; then sudo rm "$TARGET"; fi
    sudo ln -s "$SOURCE" "$TARGET"
    sudo chmod +x "$SOURCE"
}

link_binary "kiwi"
link_binary "kiwi-server"

# -----------------------------------------------------------------------------
# 7. Configure Service (kiwi-server)
# -----------------------------------------------------------------------------
printf "Configuring ${BLUE}kiwi-server${NC} service...\n"

if [ "$OS" = "Darwin" ]; then
    # --- macOS (launchd) ---
    PLIST_PATH="/Library/LaunchDaemons/com.kiwi.server.plist"

    # Create Plist file
    sudo bash -c "cat > $PLIST_PATH" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.kiwi.server</string>
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
    <string>/tmp/kiwi-server.err</string>
    <key>StandardOutPath</key>
    <string>/tmp/kiwi-server.out</string>
</dict>
</plist>
EOF

    # Set permissions and load
    sudo chown root:wheel "$PLIST_PATH"
    sudo chmod 644 "$PLIST_PATH"

    # Unload if exists (ignore error) then load
    sudo launchctl unload "$PLIST_PATH" 2>/dev/null || true
    sudo launchctl load "$PLIST_PATH"
    printf "Service registered with launchd.\n"

elif [ "$OS" = "Linux" ]; then
    # --- Linux (systemd) ---
    if command -v systemctl > /dev/null; then
        SERVICE_PATH="/etc/systemd/system/kiwi-server.service"

        sudo bash -c "cat > $SERVICE_PATH" <<EOF
[Unit]
Description=Kiwi Language Server
After=network.target

[Service]
Type=simple
ExecStart=${INSTALL_DIR}/bin/kiwi-server
WorkingDirectory=${INSTALL_DIR}
Restart=always
User=root
Group=root

[Install]
WantedBy=multi-user.target
EOF

        sudo systemctl daemon-reload
        sudo systemctl enable kiwi-server
        sudo systemctl restart kiwi-server
        printf "Service registered with systemd.\n"
    else
        printf "${RED}Warning: systemd not found. Service not configured automatically.${NC}\n"
    fi
fi

# -----------------------------------------------------------------------------
# 8. Verify
# -----------------------------------------------------------------------------
printf "${GREEN}Kiwi installed successfully!${NC}\n"
printf "Run 'kiwi' to start coding.\n"