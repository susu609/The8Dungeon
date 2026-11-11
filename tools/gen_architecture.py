#!/usr/bin/env python3
"""Sinh sơ đồ kiến trúc mã (Mermaid) từ mã nguồn Java của repo.

Không dùng AI: chỉ đọc file .java, tìm package, class và việc class nào
nhắc tới class nào, rồi ghi ra docs/ARCHITECTURE.md (GitHub tự vẽ Mermaid).

Chạy:  python3 tools/gen_architecture.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "src" / "main" / "java"
RES = ROOT / "src" / "main" / "resources"
OUT = ROOT / "docs" / "ARCHITECTURE.md"

COMMENT_RE = re.compile(r"//[^\n]*|/\*.*?\*/", re.S)
STRING_RE = re.compile(r'"(?:\\.|[^"\\])*"')
PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.M)
TYPE_RE = re.compile(r"\b(class|interface|enum|record)\s+(\w+)")
WORD_RE = re.compile(r"\b[A-Z]\w*\b")
DOC_RE = re.compile(r"/\*\*(.*?)\*/", re.S)
EVENT_RE = re.compile(r"\b(\w+(?:Event)(?:\.\w+)?)\b")


def strip(code: str) -> str:
    return STRING_RE.sub('""', COMMENT_RE.sub("", code))


def first_doc_line(raw: str, name: str) -> str:
    """Dòng đầu có chữ của Javadoc nằm ngay trước khai báo class (rỗng nếu không có)."""
    decl = next((m for m in TYPE_RE.finditer(raw) if m.group(2) == name), None)
    end = decl.start() if decl else len(raw)
    docs = [m for m in DOC_RE.finditer(raw) if m.end() <= end]
    if not docs:
        return ""
    for line in docs[-1].group(1).splitlines():
        line = re.sub(r"<[^>]+>", "", line).strip(" *\t")
        if line and not line.startswith("@"):
            return line.rstrip(":")
    return ""


def tier_of(used_by: int, top: int) -> str:
    """Mức quan trọng theo số class phụ thuộc vào nó: core / important / support."""
    if used_by >= max(3, top * 0.5):
        return "core"
    return "important" if used_by >= 2 else "support"


def scan():
    classes = {}  # tên class -> thông tin
    for f in sorted(SRC.rglob("*.java")):
        raw = f.read_text(encoding="utf-8", errors="replace")
        code = strip(raw)
        pkg = PACKAGE_RE.search(code)
        name = f.stem
        kind = next((m.group(1) for m in TYPE_RE.finditer(code) if m.group(2) == name), "class")
        classes[name] = {
            "pkg": pkg.group(1) if pkg else "",
            "kind": kind,
            "code": code,
            "lines": raw.count("\n") + 1,
            "mod": "@Mod(" in code,
            "doc": first_doc_line(raw, name),
            "events": sorted({e for e in EVENT_RE.findall(code) if e[0].isupper() and e != "SubscribeEvent"}),
        }
    return classes


def build(classes):
    base = min((c["pkg"] for c in classes.values()), key=len, default="")
    groups = {}
    for name, c in classes.items():
        sub = c["pkg"][len(base):].lstrip(".") or "(gốc)"
        groups.setdefault(sub, []).append(name)

    edges = set()
    for name, c in classes.items():
        for word in set(WORD_RE.findall(c["code"])):
            if word in classes and word != name:
                edges.add((name, word))

    nid = lambda s: re.sub(r"[^A-Za-z0-9_]", "_", s)
    out = ["```mermaid", "flowchart LR"]
    for sub in sorted(groups):
        out.append(f'  subgraph {nid("pkg_" + sub)}["{sub}"]')
        for name in sorted(groups[sub]):
            c = classes[name]
            label = f"{name}<br/><small>{c['kind']} · {c['lines']} dòng</small>"
            shape = f'(["{label}"])' if c["mod"] else f'["{label}"]'
            out.append(f"    {nid(name)}{shape}")
        out.append("  end")
    for a, b in sorted(edges):
        out.append(f"  {nid(a)} --> {nid(b)}")
    out.append("```")
    return base, groups, edges, "\n".join(out)


def main():
    classes = scan()
    base, groups, edges, mermaid = build(classes)
    total = sum(c["lines"] for c in classes.values())

    lines = [
        "# Kiến trúc mã",
        "",
        "> File này được sinh tự động bởi `tools/gen_architecture.py`. Đừng sửa tay.",
        "",
        f"- Package gốc: `{base}`",
        f"- {len(classes)} class/kiểu Java, {total} dòng, {len(edges)} phụ thuộc nội bộ",
        "",
        "Mũi tên `A --> B` nghĩa là A có dùng tới B. Khối bo tròn là entry point `@Mod`.",
        "",
        mermaid,
        "",
        "## Class theo package",
        "",
    ]
    for sub in sorted(groups):
        lines.append(f"### {sub}")
        lines.append("")
        lines.append("| Class | Loại | Dòng | Dùng tới | Event Forge |")
        lines.append("|---|---|---|---|---|")
        for name in sorted(groups[sub]):
            c = classes[name]
            deps = ", ".join(sorted(b for a, b in edges if a == name)) or "-"
            ev = ", ".join(c["events"]) or "-"
            lines.append(f"| `{name}` | {c['kind']} | {c['lines']} | {deps} | {ev} |")
        lines.append("")

    if RES.exists():
        lines += ["## Tài nguyên", ""]
        for f in sorted(RES.rglob("*")):
            if f.is_file():
                lines.append(f"- `{f.relative_to(RES).as_posix()}`")
        lines.append("")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Đã ghi {OUT.relative_to(ROOT)}")

    write_site(classes, groups, edges, mermaid, base, total)


def write_site(classes, groups, edges, mermaid, base, total):
    """Ghi site/index.html: sơ đồ + xem mã nguồn từng class (cho GitHub Pages)."""
    import json

    data = {
        "base": base,
        "total": total,
        "edges": sorted(edges),
        "groups": {k: sorted(v) for k, v in groups.items()},
        "classes": {},
    }
    used = {n: sum(1 for _, b in edges if b == n) for n in classes}
    top = max(used.values(), default=0)
    for name, c in classes.items():
        src = next(SRC.rglob(f"{name}.java"))
        data["classes"][name] = {
            "pkg": c["pkg"],
            "kind": c["kind"],
            "lines": c["lines"],
            "path": src.relative_to(ROOT).as_posix(),
            "events": c["events"],
            "mod": c["mod"],
            "doc": c["doc"],
            "usedBy": used[name],
            "tier": tier_of(used[name], top),
            "source": src.read_text(encoding="utf-8", errors="replace"),
        }
    diagram = mermaid.removeprefix("```mermaid\n").removesuffix("```")
    diagram += "\n" + "\n".join(
        f'  click {re.sub(r"[^A-Za-z0-9_]", "_", n)} call pick("{n}")' for n in classes)
    payload = json.dumps(data, ensure_ascii=False).replace("</", "<\\/")
    html = SITE_TEMPLATE.replace("__DIAGRAM__", diagram.replace("<", "&lt;").replace(">", "&gt;"))
    html = html.replace("__DATA__", payload)
    site = ROOT / "site"
    site.mkdir(exist_ok=True)
    (site / "index.html").write_text(html, encoding="utf-8")
    print("Đã ghi site/index.html")


SITE_TEMPLATE = (Path(__file__).resolve().parent / "site_template.html").read_text(encoding="utf-8")


if __name__ == "__main__":
    main()
