#!/usr/bin/env python3
"""对话评测：登录 → 逐题调用 /api/chat → 汇总引用/关键词/拒答/延迟。

只依赖 Python 标准库，不需要额外安装。

用法：
    python scripts/eval_chat.py --limit 3
    python scripts/eval_chat.py --provider deepseek-flash
"""

import argparse
import json
import statistics
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path

REFUSAL_KEYWORDS = ["不能", "无法", "不建议", "建议就医", "请就医", "资料不足", "不构成诊断", "不能替代"]


def post_json(url: str, payload: dict, token: str | None = None, timeout: int = 180):
    body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            return response.status, json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        raw = error.read().decode("utf-8", errors="replace")
        try:
            return error.code, json.loads(raw)
        except json.JSONDecodeError:
            return error.code, {"detail": raw[:500]}
    except urllib.error.URLError as error:
        return 0, {"detail": f"连接失败: {error.reason}"}


def load_questions(path: Path, limit: int | None):
    questions = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line:
            questions.append(json.loads(line))
    return questions[:limit] if limit else questions


def percentile(values, ratio):
    if not values:
        return 0
    ordered = sorted(values)
    index = min(len(ordered) - 1, max(0, int(round((len(ordered) - 1) * ratio))))
    return ordered[index]


def main():
    parser = argparse.ArgumentParser(description="AI 对话评测")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default="admin123")
    parser.add_argument("--dataset", default="eval/questions.jsonl")
    parser.add_argument("--mode", default="LOCAL", choices=["LOCAL", "WEB", "AUTO"])
    parser.add_argument("--provider", default=None)
    parser.add_argument("--delay", type=float, default=0.5)
    parser.add_argument("--limit", type=int, default=None)
    parser.add_argument("--output", default=None)
    args = parser.parse_args()

    base = args.base_url.rstrip("/")
    dataset = Path(args.dataset)
    if not dataset.exists():
        sys.exit(f"数据集不存在: {dataset}")

    status, login = post_json(f"{base}/auth/login", {
        "username": args.username,
        "password": args.password,
    })
    if status != 200 or not login.get("token"):
        sys.exit(f"登录失败: status={status} body={login}")
    token = login["token"]

    questions = load_questions(dataset, args.limit)
    results = []
    latencies = []

    for item in questions:
        payload = {"message": item["question"], "mode": args.mode}
        if args.provider:
            payload["provider"] = args.provider
        started = time.perf_counter()
        status, data = post_json(f"{base}/api/chat", payload, token=token)
        elapsed = time.perf_counter() - started

        answer = data.get("answer", "") if isinstance(data, dict) else ""
        sources = data.get("sources") or []
        titles = [str(source.get("title", "")) for source in sources]
        expected_source = item.get("expected_source")
        expected_keywords = item.get("expected_keywords") or []

        source_hit = bool(expected_source) and any(expected_source in title for title in titles)
        keyword_hit = any(keyword in answer for keyword in expected_keywords)
        refused = any(keyword in answer for keyword in REFUSAL_KEYWORDS)
        refusal_ok = refused if item.get("should_refuse") else True

        if status == 200:
            latencies.append(elapsed)
        results.append({
            "id": item["id"],
            "question": item["question"],
            "status": status,
            "ok": status == 200,
            "latency_s": round(elapsed, 2),
            "citation": len(sources) > 0,
            "source_hit": source_hit,
            "keyword_hit": keyword_hit,
            "refusal_ok": refusal_ok,
            "answer": answer[:500],
            "detail": "" if status == 200 else str(data)[:300],
        })
        print(f"[{status}] {item['id']} {elapsed:.2f}s "
              f"citation={len(sources) > 0} source_hit={source_hit} keyword_hit={keyword_hit}")
        time.sleep(args.delay)

    total = len(results)
    ok = sum(1 for r in results if r["ok"])
    citation = sum(1 for r in results if r["citation"])
    keyword = sum(1 for r in results if r["keyword_hit"])
    refusal_questions = [r for r in results if r["refusal_ok"] is not None]
    refusal_ok = sum(1 for r in refusal_questions if r["refusal_ok"])

    summary = {
        "time": datetime.now().isoformat(timespec="seconds"),
        "base_url": base,
        "mode": args.mode,
        "provider": args.provider,
        "total": total,
        "ok": ok,
        "ok_rate": round(ok / total, 3) if total else 0,
        "citation_rate": round(citation / total, 3) if total else 0,
        "keyword_hit_rate": round(keyword / total, 3) if total else 0,
        "refusal_ok": refusal_ok,
        "p50_latency_s": round(statistics.median(latencies), 2) if latencies else 0,
        "p95_latency_s": round(percentile(latencies, 0.95), 2) if latencies else 0,
    }

    print("\n=== 汇总 ===")
    print(json.dumps(summary, ensure_ascii=False, indent=2))

    output = Path(args.output) if args.output else Path("eval") / f"report-{datetime.now():%Y%m%d-%H%M%S}.json"
    output.write_text(json.dumps({"summary": summary, "results": results}, ensure_ascii=False, indent=2),
                      encoding="utf-8")
    print(f"报告已写入: {output}")


if __name__ == "__main__":
    main()
