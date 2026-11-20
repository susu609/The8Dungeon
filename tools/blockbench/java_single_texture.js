/**
 * Blockbench plugin: Java Block Export (Single Texture)
 *
 * Xuất model đang mở (Bedrock, Java Block hay Generic) thành model block Java (JSON),
 * mọi mặt dùng CHUNG một texture (ví dụ minecraft:block/stone_bricks), giữ nguyên UV từng mặt.
 *
 * Cài: Blockbench -> File -> Plugins -> nút "Load Plugin from File" -> chọn file này.
 * Dùng: File -> Export ->
 *   "Export Java Block (Single Texture)"      : model block JSON (dùng với renderSingleBlock / đặt như block)
 *   "Export Java Entity Model (Single Texture)": lớp Java vẽ bằng quad, giữ UV từng mặt (dùng cho prop entity)
 *
 * Giới hạn của model block Java (plugin báo lỗi rõ thay vì xuất sai):
 *  - khối chỉ xoay MỘT trục, góc 0 / ±22.5 / ±45; nhóm (bone) không được xoay
 *  - toạ độ nằm trong -16..32
 */
(function () {
    'use strict';

    const FACES = ['north', 'south', 'east', 'west', 'up', 'down'];
    const JAVA_ANGLES = [-45, -22.5, 0, 22.5, 45];
    const JAVA_DISPLAY = ['thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand',
        'firstperson_lefthand', 'gui', 'head', 'ground', 'fixed'];

    const r4 = v => Math.round(v * 10000) / 10000;

    /**
     * Phần lõi, không phụ thuộc Blockbench (để test được).
     * cubes: [{name, from, to, inflate, rotation, origin, faces:{dir:{uv:[u1,v1,u2,v2], texture, rotation, tint}}, parentRotations:[{name, rotation}]}]
     * opts: {texture, uvWidth, uvHeight, shift, cullface, display}
     * Trả về {model, errors, warnings}.
     */
    function buildJavaModel(cubes, opts) {
        const errors = [], warnings = [];
        const elements = [];
        for (const c of cubes) {
            for (const g of c.parentRotations || []) {
                if (g.rotation.some(a => a)) errors.push(`Nhóm "${g.name}" có xoay ${JSON.stringify(g.rotation)}: Java không xoay theo nhóm. Hãy đặt xoay của nhóm về 0.`);
            }
            const inf = c.inflate || 0;
            const from = [c.from[0] - inf + opts.shift, c.from[1] - inf, c.from[2] - inf + opts.shift].map(r4);
            const to = [c.to[0] + inf + opts.shift, c.to[1] + inf, c.to[2] + inf + opts.shift].map(r4);
            if ([...from, ...to].some(v => v < -16 || v > 32)) errors.push(`Khối "${c.name}" vượt khung -16..32 của Java.`);
            if (inf) warnings.push(`Khối "${c.name}" có inflate ${inf}: đã nới from/to (UV giữ nguyên).`);

            const el = {name: c.name, from, to};
            const rot = c.rotation || [0, 0, 0];
            const axes = [0, 1, 2].filter(i => rot[i]);
            if (axes.length > 1) {
                errors.push(`Khối "${c.name}" xoay nhiều trục ${JSON.stringify(rot)}: Java chỉ xoay một trục.`);
            } else if (axes.length === 1) {
                const i = axes[0];
                if (!JAVA_ANGLES.includes(rot[i])) errors.push(`Khối "${c.name}" xoay ${rot[i]} độ: Java chỉ nhận 0, ±22.5, ±45.`);
                const o = c.origin || [0, 0, 0];
                el.rotation = {angle: rot[i], axis: 'xyz'[i], origin: [o[0] + opts.shift, o[1], o[2] + opts.shift].map(r4)};
            }

            el.faces = {};
            for (const dir of FACES) {
                const f = c.faces[dir];
                if (!f || f.texture === null) continue;           // mặt bị tắt trong Blockbench -> không xuất
                const uv = [f.uv[0] * 16 / opts.uvWidth, f.uv[1] * 16 / opts.uvHeight,
                            f.uv[2] * 16 / opts.uvWidth, f.uv[3] * 16 / opts.uvHeight].map(r4);
                const face = {uv, texture: '#tex'};
                if (f.rotation) face.rotation = f.rotation;
                if (typeof f.tint === 'number' && f.tint >= 0) face.tintindex = f.tint;
                if (opts.cullface) {
                    const edge = {down: from[1] === 0, up: to[1] === 16, west: from[0] === 0,
                                  east: to[0] === 16, north: from[2] === 0, south: to[2] === 16};
                    if (edge[dir] && !el.rotation) face.cullface = dir;
                }
                el.faces[dir] = face;
            }
            if (Object.keys(el.faces).length === 0) warnings.push(`Khối "${c.name}" không có mặt nào được bật.`);
            elements.push(el);
        }
        const model = {
            credit: 'Made with Blockbench, exported by java_single_texture',
            parent: 'minecraft:block/block',
            textures: {particle: opts.texture, tex: opts.texture},
            elements
        };
        if (opts.display && Object.keys(opts.display).length) model.display = opts.display;
        return {model, errors, warnings};
    }

    // ---------- Xuất lớp model entity (quad, giữ UV từng mặt) ----------
    // Góc mỗi mặt theo thứ tự TL, BL, BR, TR (nhìn từ ngoài vào), khớp cách Java dán UV cho model block.
    // Dùng hàm theo (x1,y1,z1,x2,y2,z2) để tính toạ độ góc.
    const CORNERS = {
        north: (a, b) => [[b[0], b[1], a[2]], [b[0], a[1], a[2]], [a[0], a[1], a[2]], [a[0], b[1], a[2]]],
        south: (a, b) => [[a[0], b[1], b[2]], [a[0], a[1], b[2]], [b[0], a[1], b[2]], [b[0], b[1], b[2]]],
        west:  (a, b) => [[a[0], b[1], a[2]], [a[0], a[1], a[2]], [a[0], a[1], b[2]], [a[0], b[1], b[2]]],
        east:  (a, b) => [[b[0], b[1], b[2]], [b[0], a[1], b[2]], [b[0], a[1], a[2]], [b[0], b[1], a[2]]],
        up:    (a, b) => [[a[0], b[1], a[2]], [a[0], b[1], b[2]], [b[0], b[1], b[2]], [b[0], b[1], a[2]]],
        down:  (a, b) => [[a[0], a[1], b[2]], [a[0], a[1], a[2]], [b[0], a[1], a[2]], [b[0], a[1], b[2]]]
    };
    const NORMALS = {north: [0, 0, -1], south: [0, 0, 1], west: [-1, 0, 0], east: [1, 0, 0], up: [0, 1, 0], down: [0, -1, 0]};

    /**
     * Tạo danh sách quad. Toạ độ tính bằng pixel (1/16 block), gốc ở TÂM ĐÁY entity:
     * khối nằm giữa vị trí entity, khớp hitbox (không cần dịch -0.5 trong renderer).
     * opts: {uvWidth, uvHeight, shift}  (shift = -8 nếu project dùng lưới góc như Java Block, 0 nếu lưới căn giữa)
     */
    function buildEntityQuads(cubes, opts) {
        const errors = [], warnings = [], quads = [];
        for (const c of cubes) {
            for (const g of c.parentRotations || []) {
                if (g.rotation.some(x => x)) errors.push(`Nhóm "${g.name}" có xoay: bản này chưa hỗ trợ xoay khi xuất entity.`);
            }
            if ((c.rotation || [0, 0, 0]).some(x => x)) errors.push(`Khối "${c.name}" có xoay: bản này chưa hỗ trợ xoay khi xuất entity.`);
            const inf = c.inflate || 0;
            const a = [c.from[0] - inf + opts.shift, c.from[1] - inf, c.from[2] - inf + opts.shift].map(r4);
            const b = [c.to[0] + inf + opts.shift, c.to[1] + inf, c.to[2] + inf + opts.shift].map(r4);
            for (const dir of FACES) {
                const f = c.faces[dir];
                if (!f || f.texture === null) continue;
                const u1 = r4(f.uv[0] * 16 / opts.uvWidth), v1 = r4(f.uv[1] * 16 / opts.uvHeight);
                const u2 = r4(f.uv[2] * 16 / opts.uvWidth), v2 = r4(f.uv[3] * 16 / opts.uvHeight);
                let uvs = [[u1, v1], [u1, v2], [u2, v2], [u2, v1]];          // TL, BL, BR, TR
                const steps = ((f.rotation || 0) / 90) % 4;                    // xoay texture theo chiều kim đồng hồ
                for (let i = 0; i < steps; i++) uvs = [uvs[1], uvs[2], uvs[3], uvs[0]];
                const pos = CORNERS[dir](a, b);
                quads.push({cube: c.name, dir, verts: pos.map((p, i) => [...p, ...uvs[i]]), normal: NORMALS[dir]});
            }
        }
        return {quads, errors, warnings};
    }

    const jf = v => (Number.isInteger(v) ? String(v) : String(r4(v))) + 'f';

    function toPascal(name) {
        const p = String(name || 'model').replace(/\.[^.]+$/, '').split(/[^A-Za-z0-9]+/).filter(Boolean)
            .map(w => w[0].toUpperCase() + w.slice(1)).join('');
        return /^[A-Za-z]/.test(p) ? p : 'Model' + p;
    }

    /** texture "minecraft:block/stone_bricks" -> ["minecraft", "textures/block/stone_bricks.png"] */
    function textureFile(id) {
        const [ns, path] = id.includes(':') ? id.split(':') : ['minecraft', id];
        return [ns, path.startsWith('textures/') ? path : `textures/${path}.png`];
    }

    function buildEntityModelJava(cubes, opts) {
        const {quads, errors, warnings} = buildEntityQuads(cubes, opts);
        const [ns, file] = textureFile(opts.texture);
        const rows = quads.map(q => `            {${q.verts.map(v => v.map(jf).join(', ')).join(',  ')},  ${q.normal.map(jf).join(', ')}}, // ${q.cube} ${q.dir}`);
        const code = `package ${opts.pkg};

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Model "${opts.className}" sinh tự động bởi plugin Blockbench java_single_texture.
 * Đừng sửa tay: sửa trong Blockbench rồi xuất lại.
 *
 * Vẽ bằng quad nên giữ được UV từng mặt (ModelPart của vanilla chỉ có box UV).
 * Gốc toạ độ ở TÂM ĐÁY entity, trục Y hướng lên: KHÔNG lật (scale -1) như model mob thường.
 *
 * Dùng trong renderer:
 *   private final ${opts.className} model = new ${opts.className}();
 *   model.renderToBuffer(pose, buffers.getBuffer(model.renderType(${opts.className}.TEXTURE)),
 *           light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
 */
public class ${opts.className} extends Model {

    public static final ResourceLocation TEXTURE = new ResourceLocation("${ns}", "${file}");

    // Mỗi dòng là một mặt: 4 đỉnh (x, y, z, u, v) theo thứ tự trên-trái, dưới-trái, dưới-phải, trên-phải,
    // rồi pháp tuyến (nx, ny, nz). x, y, z tính bằng pixel (1/16 block); u, v trên lưới 0..16 của texture.
    private static final float[][] QUADS = {
${rows.join('\n')}
    };

    public ${opts.className}() {
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        for (float[] q : QUADS) {
            for (int i = 0; i < 4; i++) {
                int k = i * 5;
                buffer.vertex(matrix, q[k] / 16f, q[k + 1] / 16f, q[k + 2] / 16f)
                        .color(red, green, blue, alpha)
                        .uv(q[k + 3] / 16f, q[k + 4] / 16f)
                        .overlayCoords(overlay)
                        .uv2(light)
                        .normal(normal, q[20], q[21], q[22])
                        .endVertex();
            }
        }
    }
}
`;
        return {code, errors, warnings, quadCount: quads.length};
    }

    function runEntityExport(form) {
        if (!Cube.all.length) { Blockbench.showQuickMessage('Model chưa có khối nào', 2000); return; }
        const className = (form.class_name || '').trim() || toPascal(Project.name) + 'Model';
        if (!/^[A-Z][A-Za-z0-9_]*$/.test(className)) { Blockbench.showMessageBox({title: 'Tên lớp không hợp lệ', message: `"${className}" phải bắt đầu bằng chữ in hoa, chỉ gồm chữ, số, _.`}); return; }
        const opts = {
            texture: (form.texture || '').trim() || 'minecraft:block/stone_bricks',
            pkg: (form.package || '').trim() || 'net.susu609.the8dungeon.client.model',
            className,
            uvWidth: Project.texture_width || 16,
            uvHeight: Project.texture_height || 16,
            shift: Format.centered_grid ? 0 : -8    // entity: gốc ở tâm đáy
        };
        const {code, errors, warnings, quadCount} = buildEntityModelJava(collectCubes(), opts);
        if (errors.length) {
            Blockbench.showMessageBox({title: 'Không xuất được lớp model', message: errors.map(e => '• ' + e).join('\n')});
            return;
        }
        Blockbench.export({type: 'Java Class', extensions: ['java'], name: className, content: code}, () => {
            Blockbench.showQuickMessage(`Đã xuất ${className}.java (${quadCount} mặt)`, 2000);
            if (warnings.length) Blockbench.showMessageBox({title: 'Lưu ý khi xuất', message: warnings.map(n => '• ' + n).join('\n')});
        });
    }

    // ---------- Phần gắn vào Blockbench ----------
    function collectCubes() {
        return Cube.all.filter(c => c.export !== false && c.visibility !== false).map(c => {
            const parentRotations = [];
            let p = c.parent;
            while (p && p !== 'root' && p.rotation) { parentRotations.push({name: p.name, rotation: p.rotation.slice()}); p = p.parent; }
            const faces = {};
            for (const dir of FACES) {
                const f = c.faces[dir];
                if (f) faces[dir] = {uv: f.uv.slice(), texture: f.texture, rotation: f.rotation, tint: f.tint};
            }
            return {name: c.name, from: c.from.slice(), to: c.to.slice(), inflate: c.inflate,
                    rotation: c.rotation.slice(), origin: c.origin.slice(), faces, parentRotations};
        });
    }

    function collectDisplay() {
        const out = {};
        const ds = (typeof Project !== 'undefined' && Project.display_settings) || {};
        for (const slot of JAVA_DISPLAY) {
            const d = ds[slot];
            if (!d) continue;
            const v = typeof d.export === 'function' ? d.export() : {rotation: d.rotation, translation: d.translation, scale: d.scale};
            if (v && Object.keys(v).length) out[slot] = v;
        }
        return out;
    }

    function runExport(form) {
        if (!Cube.all.length) { Blockbench.showQuickMessage('Model chưa có khối nào', 2000); return; }
        const opts = {
            texture: (form.texture || '').trim() || 'minecraft:block/stone_bricks',
            uvWidth: Project.texture_width || 16,
            uvHeight: Project.texture_height || 16,
            shift: Format.centered_grid ? 8 : 0,   // Bedrock: tâm block ở 0. Java: góc block ở 0.
            cullface: !!form.cullface,
            display: form.display ? collectDisplay() : null
        };
        const {model, errors, warnings} = buildJavaModel(collectCubes(), opts);
        if (errors.length) {
            Blockbench.showMessageBox({title: 'Không xuất được sang Java', message: errors.map(e => '• ' + e).join('\n')});
            return;
        }
        Blockbench.export({
            type: 'Java Block Model',
            extensions: ['json'],
            name: (Project.name || 'model').replace(/\.[^.]+$/, ''),
            content: JSON.stringify(model, null, 2)
        }, () => {
            const usedTextures = Texture.all ? Texture.all.length : 0;
            const notes = warnings.slice();
            if (usedTextures > 1) notes.push(`Project có ${usedTextures} texture; tất cả mặt đã gộp về một texture "${opts.texture}".`);
            Blockbench.showQuickMessage(`Đã xuất ${model.elements.length} khối`, 2000);
            if (notes.length) Blockbench.showMessageBox({title: 'Lưu ý khi xuất', message: notes.map(n => '• ' + n).join('\n')});
        });
    }

    let action, dialog, entityAction, entityDialog;
    if (typeof BBPlugin !== 'undefined') {
        BBPlugin.register('java_single_texture', {
            title: 'Java Block Export (Single Texture)',
            author: 'The8Dungeon',
            icon: 'fa-cube',
            description: 'Xuất model thành model block Java dùng một texture duy nhất, giữ UV từng mặt.',
            version: '1.0.0',
            variant: 'both',
            onload() {
                dialog = new Dialog({
                    id: 'java_single_texture_dialog',
                    title: 'Export Java Block (Single Texture)',
                    form: {
                        texture: {label: 'Texture', type: 'text', value: 'minecraft:block/stone_bricks'},
                        cullface: {label: 'Tự thêm cullface ở mặt sát cạnh block', type: 'checkbox', value: true},
                        display: {label: 'Xuất display (tay, GUI, đất…)', type: 'checkbox', value: true}
                    },
                    onConfirm(form) { runExport(form); }
                });
                action = new Action('export_java_single_texture', {
                    name: 'Export Java Block (Single Texture)',
                    description: 'Model block Java, một texture, giữ UV từng mặt',
                    icon: 'fa-cube',
                    click() { dialog.show(); }
                });
                MenuBar.addAction(action, 'file.export');

                entityDialog = new Dialog({
                    id: 'java_single_texture_entity_dialog',
                    title: 'Export Java Entity Model (Single Texture)',
                    form: {
                        class_name: {label: 'Tên lớp (để trống = theo tên project)', type: 'text', value: ''},
                        package: {label: 'Package', type: 'text', value: 'net.susu609.the8dungeon.client.model'},
                        texture: {label: 'Texture', type: 'text', value: 'minecraft:block/stone_bricks'}
                    },
                    onConfirm(form) { runEntityExport(form); }
                });
                entityAction = new Action('export_java_single_texture_entity', {
                    name: 'Export Java Entity Model (Single Texture)',
                    description: 'Lớp Java vẽ bằng quad, một texture, giữ UV từng mặt (cho prop entity)',
                    icon: 'fa-cubes',
                    click() { entityDialog.show(); }
                });
                MenuBar.addAction(entityAction, 'file.export');
            },
            onunload() {
                if (action) action.delete();
                if (dialog && dialog.delete) dialog.delete();
                if (entityAction) entityAction.delete();
                if (entityDialog && entityDialog.delete) entityDialog.delete();
            }
        });
    }

    if (typeof module !== 'undefined') module.exports = {buildJavaModel, buildEntityQuads, buildEntityModelJava};
})();
