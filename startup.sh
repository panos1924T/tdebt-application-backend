#!/bin/bash

ENV_FILE=".env.docker"

set -a
. ./"$ENV_FILE"
set +a

echo "Building the project..."
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "Build failed."
    exit 1
fi

echo "Stopping and removing existing containers..."
docker compose down 2>/dev/null
if [ $? -ne 0 ]; then
    echo "No containers were running."
fi

echo "Building and starting containers..."
docker compose up -d --build
if [ $? -ne 0 ]; then
    echo "Failed to start containers."
    exit 1
fi

echo "Waiting for Spring Boot to boot up..."
MAX_RETRIES=120
RETRY=0

while [ $RETRY -lt $MAX_RETRIES ]; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080)
  if [ "$STATUS" = "200" ] || [ "$STATUS" = "401" ]; then
    echo " Server is online."
    exit 0
  fi
  RETRY=$((RETRY + 1))
  echo -n "."
  sleep 1
done

echo " Server did not become ready in time."
exit 1