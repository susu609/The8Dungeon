#!/usr/bin/env python3
"""Đọc lớp model kiểu vanilla (createBodyLayer: addOrReplaceChild / texOffs / addBox / PartPose)
và ghi ra file .bbmodel (Blockbench, Modded Entity, box UV) để mở và sửa tiếp.

Chạy:
  python3 tools/blockbench/layer_to_bbmodel.py src/.../client/model/GateModel.java tools/blockbench/models/gate.bbmodel [texture16.png]

- Part "root" ở y = 24 (lớp bọc mà plugin java_single_texture tự thêm) được bỏ qua, nên
  xuất lại bằng plugin sẽ ra đúng code cũ.
- Toạ độ đổi ngược với plugin: X = -x, Y = 24 - y, Z = z (ModelPart có Y hướng xuống, X bị lật).
- Texture: nhúng file PNG nếu đưa vào (ví dụ stone.png lấy từ jar Minecraft), không thì nhúng ô xám tạm.
- Group có xoay: giữ góc xoay, nhưng toạ độ con chỉ đúng khi group cha không xoay (báo cảnh báo).
"""
import base64
import io
import json
import re
import sys
import uuid

NUM = r"-?\d+(?:\.\d+)?"


def floats(s):
    return [float(x) for x in re.findall(NUM + r"(?=F|f|\b)", s)]


def parse(java):
    m = re.search(r"LayerDefinition\.create\(\s*\w+\s*,\s*(\d+)\s*,\s*(\d+)\s*\)", java)
    tex_w, tex_h = (int(m.group(1)), int(m.group(2))) if m else (64, 64)
    tm = re.search(r'TEXTURE\s*=\s*ResourceLocation\.\w+\(\s*"([^"]+)"\s*,\s*"([^"]+)"', java)
    texture = f"{tm.group(1)}:{tm.group(2)}" if tm else "texture"
    body = java[java.index("createBodyLayer"):]
    stmt = re.compile(
        r'(?:PartDefinition\s+(\w+)\s*=\s*)?(\w+)\.addOrReplaceChild\(\s*"(\w+)"\s*,\s*CubeListBuilder\.create\(\)(.*?),\s*'
        r'PartPose\.(offset|offsetAndRotation)\(([^)]*)\)\s*\)\s*;', re.S)
    parts = {}  # tên biến -> part
    roots = []
    for var, parent, name, cubes_src, kind, pose in stmt.findall(body):
        vals = floats(pose)
        off, rot = vals[:3], (vals[3:6] if kind == "offsetAndRotation" else [0.0, 0.0, 0.0])
        p_parent = parts.get(parent)
        pivot = [a + b for a, b in zip(p_parent["pivot"] if p_parent else [0, 0, 0], off)]
        cubes = []
        for cm in re.finditer(
                r"\.texOffs\(\s*(\d+)\s*,\s*(\d+)\s*\)(\.mirror\(\))?\s*\.addBox\(([^)]*?)(?:,\s*new CubeDeformation\(\s*(" + NUM + r")F?\s*\))?\)",
                cubes_src):
            a = floats(cm.group(4))[:6]
            inflate = float(cm.group(5)) if cm.group(5) else 0.0
            name_m = re.search(r"/[/*]\s*(\w+)", cubes_src[cm.end():cm.end() + 60])
            cubes.append({"uv": [int(cm.group(1)), int(cm.group(2))], "mirror": bool(cm.group(3)),
                          "box": a, "inflate": inflate, "name": name_m.group(1) if name_m else name})
        part = {"name": name, "pivot": pivot, "rot": rot, "cubes": cubes, "children": [],
                "rotated_parent": bool(p_parent and (any(p_parent["rot"]) or p_parent["rotated_parent"]))}
        (p_parent["children"] if p_parent else roots).append(part)
        if var:
            parts[var] = part
        parts.setdefault(name, part)
    return roots, tex_w, tex_h, texture


def nz(v):
    """Bỏ -0.0 và .0 thừa: -0.0 -> 0, 16.0 -> 16."""
    return [int(x) if float(x).is_integer() else x for x in (x + 0.0 for x in v)]


def to_bbmodel(roots, tex_w, tex_h, texture, png_bytes, name):
    elements, warn = [], []

    def cube_el(c, pivot_m):
        x, y, z, w, h, d = c["box"]
        fx, fy, fz = pivot_m[0] + x, pivot_m[1] + y, pivot_m[2] + z
        tx, ty, tz = fx + w, fy + h, fz + d
        el = {"name": c["name"], "uuid": str(uuid.uuid4()), "box_uv": True, "rescale": False, "color": 0,
              "from": nz([-tx, 24 - ty, fz]), "to": nz([-fx, 24 - fy, tz]),
              "origin": nz([-pivot_m[0], 24 - pivot_m[1], pivot_m[2]]),
              "uv_offset": c["uv"], "inflate": c["inflate"], "mirror_uv": c["mirror"], "faces": {}}
        elements.append(el)
        return el["uuid"]

    def group(p):
        if p["rotated_parent"]:
            warn.append(f'part "{p["name"]}" nằm trong part cha có xoay: toạ độ có thể lệch')
        import math
        rot = [-math.degrees(p["rot"][0]), -math.degrees(p["rot"][1]), math.degrees(p["rot"][2])]
        return {"name": p["name"], "uuid": str(uuid.uuid4()),
                "origin": nz([-p["pivot"][0], 24 - p["pivot"][1], p["pivot"][2]]),
                "rotation": nz([round(r, 4) for r in rot]),
                "children": [cube_el(c, p["pivot"]) for c in p["cubes"]] + [group(ch) for ch in p["children"]]}

    top = []
    for r in roots:  # bỏ lớp bọc "root" (0, 24, 0) không có hộp: plugin sẽ tự thêm lại
        if r["name"] == "root" and not r["cubes"] and r["pivot"] == [0.0, 24.0, 0.0] and not any(r["rot"]):
            top += [group(ch) for ch in r["children"]]
        else:
            top.append(group(r))
    bb = {"meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True},
          "name": name, "model_identifier": name, "modded_entity_version": "1.17",
          "resolution": {"width": tex_w, "height": tex_h}, "elements": elements, "outliner": top,
          "textures": [{"name": texture.split("/")[-1], "id": "0", "width": tex_w, "height": tex_h,
                        "uv_width": tex_w, "uv_height": tex_h, "uuid": str(uuid.uuid4()),
                        "source": "data:image/png;base64," + base64.b64encode(png_bytes).decode()}]}
    return bb, warn


def placeholder_png(w, h):
    try:
        from PIL import Image
    except ImportError:
        sys.exit("cần Pillow để tạo texture tạm, hoặc truyền file PNG vào tham số thứ 3")
    im = Image.new("RGBA", (w, h), (128, 128, 128, 255))
    for yy in range(h):
        for xx in range(w):
            if (xx // 4 + yy // 4) % 2: im.putpixel((xx, yy), (110, 110, 110, 255))
    buf = io.BytesIO();
    im.save(buf, "PNG");
    return buf.getvalue()


if __name__ == "__main__":
    if len(sys.argv) not in (3, 4):
        sys.exit(__doc__)
    java = open(sys.argv[1], encoding="utf-8").read()
    roots, tw, th, tex = parse(java)
    if not roots:
        sys.exit("không tìm thấy addOrReplaceChild trong createBodyLayer")
    png = open(sys.argv[3], "rb").read() if len(sys.argv) == 4 else placeholder_png(tw, th)
    name = re.sub(r"(?<!^)(?=[A-Z])", "_", re.search(r"class\s+(\w+?)Model\b", java).group(1)).lower()
    bb, warn = to_bbmodel(roots, tw, th, tex, png, name)
    json.dump(bb, open(sys.argv[2], "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    print(f"đã ghi {sys.argv[2]}: {len(bb['elements'])} hộp, texture {tw}x{th} ({tex})")
    for w_ in warn: print("cảnh báo:", w_)
