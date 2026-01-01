#!/bin/bash

# ==========================================
# Kiwi Programming Language Installer
# ==========================================

set -e

KIWI_VERSION="0.0.1"
REPO="kiwi-language/kiwi"
INSTALL_DIR="$HOME/.kiwi"
BIN_LINK_DIR="$HOME/.local/bin"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
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
# Handle nested folder if zip contains a root folder
if [ $(ls -1 "$EXTRACT_DIR" | wc -l) -eq 1 ]; then
    NESTED_DIR=$(ls -1 "$EXTRACT_DIR")
    if [ -d "$EXTRACT_DIR/$NESTED_DIR" ]; then
        SOURCE_DIR="$EXTRACT_DIR/$NESTED_DIR"
    fi
fi

# -----------------------------------------------------------------------------
# 4. Install Files
# -----------------------------------------------------------------------------
printf "Installing to ${INSTALL_DIR}...\n"

# Remove previous install if exists
if [ -d "$INSTALL_DIR" ]; then rm -rf "$INSTALL_DIR"; fi

mkdir -p "$INSTALL_DIR"
cp -R "$SOURCE_DIR/"* "$INSTALL_DIR/"
rm -rf "$TEMP_DIR"

# -----------------------------------------------------------------------------
# 5. Link Binaries
# -----------------------------------------------------------------------------
printf "Linking binaries to ${BIN_LINK_DIR}...\n"
mkdir -p "$BIN_LINK_DIR"

link_binary() {
    local BIN_NAME=$1
    local SOURCE="${INSTALL_DIR}/bin/${BIN_NAME}"
    local TARGET="${BIN_LINK_DIR}/${BIN_NAME}"

    if [ ! -f "$SOURCE" ]; then printf "${RED}Error: Binary $SOURCE missing.${NC}\n"; exit 1; fi
    # Remove existing link/file if it exists
    if [ -f "$TARGET" ] || [ -L "$TARGET" ]; then rm "$TARGET"; fi

    ln -s "$SOURCE" "$TARGET"
    chmod +x "$SOURCE"
}

link_binary "kiwi"
link_binary "kiwi-server"

# -----------------------------------------------------------------------------
# 6. Configure Service
# -----------------------------------------------------------------------------
printf "Configuring ${BLUE}kiwi-server${NC} as a service...\n"

if [ "$OS" = "Darwin" ]; then
    # --- macOS: LaunchAgents (User specific) ---
    LAUNCH_AGENT_DIR="$HOME/Library/LaunchAgents"
    PLIST_PATH="${LAUNCH_AGENT_DIR}/com.kiwi.server.plist"
    LOG_DIR="$HOME/Library/Logs/Kiwi"
    mkdir -p "$LAUNCH_AGENT_DIR"
    mkdir -p "$LOG_DIR"

    cat > "$PLIST_PATH" <<EOF
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
    <string>${LOG_DIR}/kiwi-server.log</string>
    <key>StandardOutPath</key>
    <string>${LOG_DIR}/kiwi-server.log</string>
</dict>
</plist>
EOF
    # Reload service
    launchctl unload "$PLIST_PATH" 2>/dev/null || true
    launchctl load "$PLIST_PATH"

elif [ "$OS" = "Linux" ]; then
    # --- Linux: systemd --user ---
    if command -v systemctl > /dev/null; then
        SYSTEMD_USER_DIR="$HOME/.config/systemd/user"
        SERVICE_PATH="${SYSTEMD_USER_DIR}/kiwi-server.service"
        mkdir -p "$SYSTEMD_USER_DIR"

        cat > "$SERVICE_PATH" <<EOF
[Unit]
Description=Kiwi Language Server (User)
After=network.target

[Service]
Type=simple
ExecStart=${INSTALL_DIR}/bin/kiwi-server
WorkingDirectory=${INSTALL_DIR}
Restart=always
# No User/Group needed, runs as current user

[Install]
WantedBy=default.target
EOF
        # Reload systemd user daemon
        systemctl --user daemon-reload
        systemctl --user enable kiwi-server
        systemctl --user restart kiwi-server

        # Ensure user services run even if user isn't logged in (optional)
        # loginctl enable-linger $USER 2>/dev/null || true
    else
        printf "${YELLOW}Warning: systemd not found. Service not started automatically.${NC}\n"
    fi
fi

# -----------------------------------------------------------------------------
# 7. Post-Install Instructions
# -----------------------------------------------------------------------------
printf "${GREEN}Kiwi installed successfully!${NC}\n"
printf "Location: ${INSTALL_DIR}\n"

# Check if ~/.local/bin is in PATH
case ":$PATH:" in
    *":$BIN_LINK_DIR:"*) ;;
    *)
        printf "${YELLOW}NOTE:${NC} Please add ${BIN_LINK_DIR} to your PATH.\n"
        printf "Run this command or add it to your shell config (~/.bashrc, ~/.zshrc):\n"
        printf "${BLUE}export PATH=\"\$PATH:${BIN_LINK_DIR}\"${NC}\n"
        ;;
esac

printf "Service is running as current user: $(whoami)\n"