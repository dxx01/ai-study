#!/usr/bin/env bash
# 在阿里云 ECS 上已 SSH 登录后执行（root）：
#   curl -fsSL https://raw.githubusercontent.com/dxx01/ai-study/main/deploy/bootstrap-on-server.sh | bash
# 或先 git clone 再： bash /opt/ai-study/deploy/bootstrap-on-server.sh
set -euo pipefail

REPO_DIR="${REPO_DIR:-/opt/ai-study}"
DEPLOY_DIR="$REPO_DIR/deploy"

install_docker() {
  if command -v docker >/dev/null 2>&1; then
    return
  fi
  curl -fsSL https://get.docker.com | sh
  systemctl enable --now docker
}

ensure_repo() {
  if [[ -f "$DEPLOY_DIR/docker-compose.server.yml" ]]; then
    return
  fi
  command -v git >/dev/null 2>&1 || {
    if command -v yum >/dev/null 2>&1; then yum install -y git
    else apt-get update -qq && apt-get install -y git
    fi
  }
  mkdir -p "$(dirname "$REPO_DIR")"
  if [[ ! -d "$REPO_DIR/.git" ]]; then
    git clone --depth 1 https://github.com/dxx01/ai-study.git "$REPO_DIR"
  else
    cd "$REPO_DIR" && git pull --ff-only
  fi
}

main() {
  install_docker
  ensure_repo
  cd "$DEPLOY_DIR"
  if [[ ! -f env/server.env ]]; then
    echo "请先创建 deploy/env/server.env（应用与 Docker 共用）" >&2
    exit 1
  fi
  docker compose --env-file env/server.env -f docker-compose.server.yml up -d
  docker compose --env-file env/server.env -f docker-compose.server.yml ps
  echo ""
  echo "MySQL: $(hostname -I | awk '{print $1}'):3306  库 ai_platform  用户 ai  密码见 env/server.env"
  echo "Redis: $(hostname -I | awk '{print $1}'):6379  Nacos: :8848  见 env/server.env"
}

main "$@"
