#!/bin/bash
set -e

echo "Stopping Manul server..."
launchctl stop com.manul.server 2>/dev/null || true
launchctl unload ~/Library/LaunchAgents/com.manul.server.plist 2>/dev/null || true

echo "Removing old installation..."
rm -rf $HOME/.manul

echo "Deploying new package..."
unzip -q -d $HOME dist/target/manul.zip
mv $HOME/manul $HOME/.manul

echo "Copying configuration..."
cp -f /etc/manul/manul.yml $HOME/.manul/conf

echo "Starting Manul server..."
launchctl load ~/Library/LaunchAgents/com.manul.server.plist

echo "Waiting for server to start..."
sleep 5

echo "Checking server status..."
if lsof -i :8080 >/dev/null 2>&1; then
    echo "✓ Manul server deployed and running on port 8080"
else
    echo "✗ Warning: Server may not be running on port 8080"
    exit 1
fi
