#!/usr/bin/env bash
set -euo pipefail

APP_NAME="bcapi"
IMAGE_NAME="bcapi:0.1.0"
APP_PORT="8080"

echo "Building Docker image..."
docker build -t "$IMAGE_NAME" .

echo "Stopping existing container if it exists..."
docker rm -f "$APP_NAME" >/dev/null 2>&1 || true

echo "Starting Docker container..."
docker run -d \
  --name "$APP_NAME" \
  -p "$APP_PORT:8080" \
  "$IMAGE_NAME"

echo "Application is running at http://localhost:$APP_PORT"
echo "View logs with: docker logs -f $APP_NAME"
echo "Stop with: docker stop $APP_NAME"