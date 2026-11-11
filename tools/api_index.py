"""Trích mã API Minecraft / Forge cho các ký hiệu mà mod import.

Nguồn là các sources jar do công cụ build (ModDevGradle) tạo ra khi chạy
`./gradlew createMinecraftArtifacts`, hoặc các jar chỉ định qua biến môi
trường API_SOURCES_JAR (nhiều đường dẫn, ngăn bằng dấu ':' trên Linux).

- Forge (net.minecraftforge.*): mã nguồn mở LGPL, lấy nguyên file.
- Minecraft (net.minecraft.*, com.mojang.*): mã của Mojang không được phát
  hành, chỉ giữ khai báo (class, field, chữ ký method), bỏ thân hàm.

Không tìm thấy jar nào thì trả về rỗng; trang vẫn sinh bình thường.
"""
import os
import re
import zipfile
from pathlib import Path

MAX_CHARS = 60_000


def find_jars(root: Path):
    jars = []
    for p in os.environ.get("API_SOURCES_JAR", "").split(os.pathsep):
        if p and Path(p).is_file():
            jars.append(Path(p))
    art = root / "build" / "moddev" / "artifacts"
    if art.is_dir():
        jars += sorted(art.glob("*sources*.jar"))
    cache = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1" / "net.minecraftforge"
    if cache.is_dir():
        jars += sorted(cache.rglob("forge-*-sources.jar"))
    seen, out = set(), []
    for j in jars:
        if j.resolve() not in seen:
            seen.add(j.resolve())
            out.append(j)
    return out


def strip_bodies(src: str) -> str:
    """Giữ khai báo ở cấp class, thay thân method / khối lồng bằng { … }, bỏ comment và import."""
    out, depth, i, n = [], 0, 0, len(src)
    while i < n:
        ch = src[i]
        if src.startswith("//", i):
            j = src.find("\n", i)
            i = n if j < 0 else j
            continue
        if src.startswith("/*", i):
            j = src.find("*/", i + 2)
            i = n if j < 0 else j + 2
            continue
        if ch in "\"'":
            if src.startswith('"""', i):
                j = src.find('"""', i + 3)
                end = n if j < 0 else j + 3
            else:
                j = i + 1
                while j < n and src[j] != ch:
                    j += 2 if src[j] == "\\" else 1
                end = min(n, j + 1)
            if depth <= 1:
                out.append(src[i:end])
            i = end
            continue
        if ch == "{":
            depth += 1
            if depth == 2:
                out.append("{ … }")
            elif depth < 2:
                out.append(ch)
        elif ch == "}":
            if depth <= 1:
                out.append(ch)
            depth -= 1
        elif depth <= 1:
            out.append(ch)
        i += 1
    text = "".join(out)
    text = re.sub(r"^\s*import\s+[^;]+;\s*\n", "", text, flags=re.M)
    text = re.sub(r"[ \t]+\n", "\n", text)
    return re.sub(r"\n{3,}", "\n\n", text).strip() + "\n"


def _read(jars, path):
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as z:
                if path in z.namelist():
                    return z.read(path).decode("utf-8", errors="replace"), jar.name
        except zipfile.BadZipFile:
            continue
    return None, None


def extract(root: Path, wanted: dict):
    """wanted: {tên đầy đủ: 'mc' | 'forge'} -> ({tên đầy đủ: thông tin}, [tên jar đã dùng])."""
    jars = find_jars(root)
    if not jars:
        return {}, []
    api, used = {}, set()
    for fq, kind in sorted(wanted.items()):
        parts = fq.split(".")
        # Thử chính nó, rồi bỏ dần đuôi (import static thành viên, hoặc class lồng).
        for cut in range(len(parts), 1, -1):
            cls = ".".join(parts[:cut])
            src, jar = _read(jars, cls.replace(".", "/") + ".java")
            if src is not None:
                break
        if src is None:
            continue
        used.add(jar)
        mode = "source" if kind == "forge" else "signature"
        text = src if mode == "source" else strip_bodies(src)
        if len(text) > MAX_CHARS:
            text = text[:MAX_CHARS] + "\n// … (cắt bớt cho gọn)\n"
        api[fq] = {"kind": kind, "cls": cls, "mode": mode, "jar": jar, "text": text}
    return api, sorted(used)
