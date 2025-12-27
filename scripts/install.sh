#!/bin/bash

# ==========================================
# Kiwi Programming Language Installer
# ==========================================

set -e # Exit immediately if a command exits with a non-zero status

KIWI_VERSION="0.0.3"
REPO="kiwi-language/kiwi"
INSTALL_DIR="/usr/local/kiwi"
BIN_LINK_DIR="/usr/local/bin"

# Colors (Standard ANSI)
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
    # Darwin is macOS
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
# 2. Check for dependencies
# -----------------------------------------------------------------------------
if ! command -v curl > /dev/null; then
    printf "${RED}Error: curl is required to download Kiwi.${NC}\n"
    exit 1
fi
if ! command -v unzip > /dev/null; then
    printf "${RED}Error: unzip is required to extract Kiwi.${NC}\n"
    exit 1
fi

# -----------------------------------------------------------------------------
# 3. Download and Extract
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
# Check if the zip had a single root folder (e.g. kiwi/) or if files were loose
# We want the content inside INSTALL_DIR, not INSTALL_DIR/kiwi/
SOURCE_DIR="$EXTRACT_DIR"
if [ $(ls -1 "$EXTRACT_DIR" | wc -l) -eq 1 ]; then
    NESTED_DIR=$(ls -1 "$EXTRACT_DIR")
    if [ -d "$EXTRACT_DIR/$NESTED_DIR" ]; then
        SOURCE_DIR="$EXTRACT_DIR/$NESTED_DIR"
    fi
fi

# -----------------------------------------------------------------------------
# 5. Install to /usr/local/kiwi
# -----------------------------------------------------------------------------
printf "Installing to ${INSTALL_DIR}...\n"

# Remove old installation if exists
if [ -d "$INSTALL_DIR" ]; then
    sudo rm -rf "$INSTALL_DIR"
fi

sudo mkdir -p "$INSTALL_DIR"

# Move files from temp source to final destination
# We use cp -R to handle copying contents safely with sudo
sudo cp -R "$SOURCE_DIR/"* "$INSTALL_DIR/"

# Cleanup temp
rm -rf "$TEMP_DIR"

# -----------------------------------------------------------------------------
# 6. Link Binaries to PATH
# -----------------------------------------------------------------------------
printf "Linking binaries to PATH...\n"

link_binary() {
    local BIN_NAME=$1
    local SOURCE="${INSTALL_DIR}/bin/${BIN_NAME}"
    local TARGET="${BIN_LINK_DIR}/${BIN_NAME}"

    # Check if the actual binary exists
    if [ ! -f "$SOURCE" ]; then
        printf "${RED}Error: Could not find binary at $SOURCE${NC}\n"
        printf "Please check the package structure.\n"
        exit 1
    fi

    # Remove existing link/file
    if [ -f "$TARGET" ] || [ -L "$TARGET" ]; then
        sudo rm "$TARGET"
    fi

    sudo ln -s "$SOURCE" "$TARGET"
    sudo chmod +x "$SOURCE"
}

link_binary "kiwi"
link_binary "kiwi-server"

# -----------------------------------------------------------------------------
# 7. Verify Installation
# -----------------------------------------------------------------------------
printf "${GREEN}Kiwi installed successfully!${NC}\n"

if command -v kiwi > /dev/null; then
    printf "Location: $(which kiwi)\n"
else
    printf "${RED}Warning: 'kiwi' command not found in PATH.${NC}\n"
    printf "Please ensure ${BIN_LINK_DIR} is in your PATH.\n"
fi