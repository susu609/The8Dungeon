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
EVENT_RE = re.compile(r"\b(\w+(?:Event)(?:\.\w+)?)\b")


def strip(code: str) -> str:
    return STRING_RE.sub('""', COMMENT_RE.sub("", code))


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
    for name, c in classes.items():
        src = next(SRC.rglob(f"{name}.java"))
        data["classes"][name] = {
            "pkg": c["pkg"],
            "kind": c["kind"],
            "lines": c["lines"],
            "path": src.relative_to(ROOT).as_posix(),
            "events": c["events"],
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


SITE_TEMPLATE = r"""<!doctype html>
<html lang="vi"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Kiến trúc mã The8Dungeon</title>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github-dark.min.css">
<style>
:root{--bg:#f6f7f9;--panel:#fff;--ink:#1d2330;--muted:#667085;--line:#e3e6ec;--accent:#3b6fd8;--chip:#eef2fb}
@media (prefers-color-scheme:dark){:root{--bg:#0f1218;--panel:#171b24;--ink:#e6e9ef;--muted:#98a2b3;--line:#2a3140;--accent:#7aa2ff;--chip:#1f2636}}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.5 system-ui,-apple-system,"Segoe UI",sans-serif}
header{padding:20px 24px 12px}
h1{margin:0;font-size:26px}
.sub{color:var(--muted);margin-top:4px}
.stats{display:flex;gap:10px;flex-wrap:wrap;margin-top:12px}
.stat{background:var(--panel);border:1px solid var(--line);border-radius:10px;padding:8px 14px}
.stat b{font-size:20px;display:block}
main{display:grid;grid-template-columns:260px 1fr;gap:16px;padding:0 24px 24px}
@media (max-width:900px){main{grid-template-columns:1fr;padding:0 16px 16px}header{padding:16px}}
aside,.card{background:var(--panel);border:1px solid var(--line);border-radius:12px}
aside{padding:12px;align-self:start;position:sticky;top:12px;max-height:calc(100vh - 24px);overflow:auto}
aside input{width:100%;padding:8px 10px;border:1px solid var(--line);border-radius:8px;background:var(--bg);color:var(--ink)}
.grp{margin-top:12px;font-size:12px;text-transform:uppercase;letter-spacing:.05em;color:var(--muted)}
.item{display:flex;justify-content:space-between;padding:5px 8px;border-radius:6px;cursor:pointer}
.item:hover{background:var(--chip)}.item.on{background:var(--accent);color:#fff}
.item small{opacity:.7}
.card{padding:16px;overflow:auto}
.tabs{display:flex;gap:6px;margin-bottom:12px}
.tabs button{border:1px solid var(--line);background:var(--panel);color:var(--ink);padding:6px 14px;border-radius:8px;cursor:pointer}
.tabs button.on{background:var(--accent);color:#fff;border-color:var(--accent)}
#diagram svg{max-width:100%;height:auto}
#diagram .node{cursor:pointer}
.meta{display:flex;flex-wrap:wrap;gap:6px;margin:8px 0}
.chip{background:var(--chip);border-radius:999px;padding:2px 10px;font-size:13px;cursor:pointer}
pre{margin:0;border-radius:10px;overflow:auto;max-height:70vh}
pre code{font-size:13px}
.empty{color:var(--muted)}
</style></head><body>
<header>
<h1>The8Dungeon · kiến trúc mã</h1>
<div class="sub">Sinh tự động từ nhánh main bởi <code>tools/gen_architecture.py</code>. Bấm vào một class để xem mã.</div>
<div class="stats" id="stats"></div>
</header>
<main>
<aside><input id="q" placeholder="Tìm class…"><div id="list"></div></aside>
<section>
<div class="tabs"><button id="tDiagram" class="on">Sơ đồ</button><button id="tCode">Mã nguồn</button></div>
<div class="card" id="vDiagram"><div id="diagram" class="mermaid">__DIAGRAM__</div></div>
<div class="card" id="vCode" hidden><div id="code" class="empty">Chọn một class ở danh sách hoặc trên sơ đồ.</div></div>
</section>
</main>
<script id="data" type="application/json">__DATA__</script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/java.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10.9.1/dist/mermaid.min.js"></script>
<script>
const D = JSON.parse(document.getElementById('data').textContent);
const $ = id => document.getElementById(id);
const el = (t, cls, txt) => { const e = document.createElement(t); if (cls) e.className = cls; if (txt != null) e.textContent = txt; return e; };
const names = Object.keys(D.classes);
[['Class', names.length], ['Dòng Java', D.total], ['Phụ thuộc', D.edges.length], ['Package', Object.keys(D.groups).length]]
  .forEach(([k, v]) => { const s = el('div', 'stat'); s.append(el('b', null, v), k); $('stats').append(s); });

function drawList(filter) {
  const list = $('list'); list.textContent = '';
  for (const g of Object.keys(D.groups).sort()) {
    const items = D.groups[g].filter(n => n.toLowerCase().includes(filter));
    if (!items.length) continue;
    list.append(el('div', 'grp', g));
    for (const n of items) {
      const it = el('div', 'item' + (n === current ? ' on' : ''));
      it.append(el('span', null, n), el('small', null, D.classes[n].lines));
      it.onclick = () => pick(n);
      list.append(it);
    }
  }
}
let current = null;
function show(tab) {
  $('vDiagram').hidden = tab !== 'diagram'; $('vCode').hidden = tab !== 'code';
  $('tDiagram').classList.toggle('on', tab === 'diagram'); $('tCode').classList.toggle('on', tab === 'code');
}
function chips(label, list) {
  const box = el('div', 'meta'); box.append(el('span', null, label));
  if (!list.length) box.append(el('span', 'empty', '—'));
  list.forEach(n => { const c = el('span', 'chip', n); if (D.classes[n]) c.onclick = () => pick(n); box.append(c); });
  return box;
}
window.pick = function (n) {
  current = n; const c = D.classes[n]; const box = $('code'); box.className = ''; box.textContent = '';
  box.append(el('h2', null, n));
  box.append(el('div', 'sub', `${c.kind} · ${c.lines} dòng · ${c.path}`));
  box.append(chips('Dùng tới:', D.edges.filter(e => e[0] === n).map(e => e[1])));
  box.append(chips('Được dùng bởi:', D.edges.filter(e => e[1] === n).map(e => e[0])));
  if (c.events.length) box.append(chips('Event Forge:', c.events));
  const pre = el('pre'); const code = el('code', 'language-java', c.source); pre.append(code); box.append(pre);
  if (window.hljs) hljs.highlightElement(code);
  show('code'); drawList($('q').value.toLowerCase());
};
$('q').oninput = e => drawList(e.target.value.toLowerCase());
$('tDiagram').onclick = () => show('diagram'); $('tCode').onclick = () => show('code');
drawList('');
const dark = matchMedia('(prefers-color-scheme: dark)').matches;
mermaid.initialize({ startOnLoad: true, securityLevel: 'loose', theme: dark ? 'dark' : 'default', flowchart: { htmlLabels: true } });
</script>
</body></html>
"""


if __name__ == "__main__":
    main()
