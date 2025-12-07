#!/bin/bash

TARGET="$1"


if [ -z "$TARGET" ]; then
  echo "Usage: ./deploy.sh <service-name>"
  exit 1
fi

cd /home/ec2-user/tech-gather
export DOCKER_REPO="${DOCKER_REPO}"

echo "Stopping $TARGET..."
docker-compose stop $TARGET

echo "Removing $TARGET container..."
docker-compose rm -f $TARGET

echo "Removing old $TARGET images..."
docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" \
 | awk -v target="$DOCKER_REPO:${TARGET}" '$1 ~ "^"target {print $2}' \
 | xargs -r docker rmi -f

echo "Pulling latest image.. "
docker-compose pull $TARGET

echo "Starting $TARGET..."
docker-compose up -d $TARGET

CONFIG="/home/ec2-user/tech-gather/services.json"
PORT=$(jq -r ".\"$TARGET\".port" $CONFIG)
HEALTH_PATH=$(jq -r ".\"$TARGET\".health" $CONFIG)

HEALTH_URL="http://localhost:${PORT}${HEALTH_PATH}"

for i in {1..20}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL")
  if [[ "$STATUS" == "200" ]]; then
    echo "Health check OK!"
    exit 0
  fi
  echo "Health check failed ($STATUS)… retry $i/20"
  sleep 3
done

echo "Deployment completed"
