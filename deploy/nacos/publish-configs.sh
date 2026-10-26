#!/usr/bin/env bash
# 将 deploy/nacos/configs/*.yaml 发布到 Nacos（在服务器上执行）
# 用法：
#   NACOS_USERNAME=nacos NACOS_PASSWORD=nacos bash publish-configs.sh
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NACOS_ADDR="${NACOS_ADDR:-http://127.0.0.1:8848}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"

TOKEN=$(curl -fsS -X POST "$NACOS_ADDR/nacos/v1/auth/login" \
  --data-urlencode "username=$NACOS_USERNAME" \
  --data-urlencode "password=$NACOS_PASSWORD" |
  sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

if [ -z "$TOKEN" ]; then
  echo "Nacos 登录失败，请检查 NACOS_USERNAME / NACOS_PASSWORD" >&2
  exit 1
fi

for file in "$DIR"/configs/*.yaml; do
  data_id="$(basename "$file")"
  echo "publish $data_id"
  curl -fsS -X POST "$NACOS_ADDR/nacos/v1/cs/configs?accessToken=$TOKEN" \
    --data-urlencode "dataId=$data_id" \
    --data-urlencode "group=DEFAULT_GROUP" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@$file" >/dev/null
done

echo "done. list:"
curl -fsS "$NACOS_ADDR/nacos/v1/cs/configs?search=accurate&dataId=&group=&pageNo=1&pageSize=50&accessToken=$TOKEN" | head -c 2000
echo
