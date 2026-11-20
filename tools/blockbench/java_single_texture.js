/**
 * Blockbench plugin: Java Block Export (Single Texture)
 *
 * Xuất model đang mở (Bedrock, Java Block hay Generic) thành model block Java (JSON),
 * mọi mặt dùng CHUNG một texture (ví dụ minecraft:block/stone_bricks), giữ nguyên UV từng mặt.
 *
 * Cài: Blockbench -> File -> Plugins -> nút "Load Plugin from File" -> chọn file này.
 * Dùng: File -> Export ->
 *   "Export Java Block (Single Texture)"      : model block JSON (dùng với renderSingleBlock / đặt như block)
 *   "Export Java Entity Model (Vanilla Box UV)": lớp model kiểu vanilla (createBodyLayer, texOffs/addBox, PartPose)
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

    // ---------- Xuất lớp model entity kiểu vanilla (box UV) ----------
    //
    // Đổi toạ độ Blockbench (Y lên, X hướng đông) sang ModelPart (Y xuống, X bị lật vì renderer scale(-1,-1,1)):
    //   pivot của part      : x_m = -x, y_m = 24 - y, z_m = z   (root ở y = 24 = mặt đất, như mọi model mob vanilla)
    //   offset so với cha   : (cha.x - x, cha.y - y, z - cha.z)
    //   hộp so với pivot    : addBox(pivot.x - to.x, pivot.y - to.y, from.z - pivot.z, rộng, cao, sâu)
    //   góc xoay của group  : (-rx, -ry, rz) đổi sang radian
    const BASES = {
        HierarchicalModel: 'HierarchicalModel (mặc định, giống FrogModel)',
        EntityModel: 'EntityModel (tự gọi root.render)',
        Model: 'Model (không gắn entity)'
    };
    const RENDER_TYPES = {
        entityCutoutNoCull: 'entityCutoutNoCull (mặc định)',
        entityCutout: 'entityCutout',
        entitySolid: 'entitySolid',
        entityTranslucent: 'entityTranslucent'
    };
    const JAVA_KEYWORDS = new Set(['abstract','assert','boolean','break','byte','case','catch','char','class','const','continue',
        'default','do','double','else','enum','extends','final','finally','float','for','goto','if','implements','import',
        'instanceof','int','interface','long','native','new','package','private','protected','public','return','short',
        'static','strictfp','super','switch','synchronized','this','throw','throws','transient','try','void','volatile','while',
        'true','false','null','var','record','yield','mesh','parts']);

    const ff = v => { const x = r4(v) + 0; return (Number.isInteger(x) ? x.toFixed(1) : String(x)) + 'F'; };
    const rad = d => ff(d * Math.PI / 180);

    function toPascal(name) {
        const p = String(name || 'model').replace(/\.[^.]+$/, '').split(/[^A-Za-z0-9]+/).filter(Boolean)
            .map(w => w[0].toUpperCase() + w.slice(1)).join('');
        return /^[A-Za-z]/.test(p) ? p : 'Model' + p;
    }
    /** "pedestal" / "PedestalModel" / "my pedestal" -> "PedestalModel" (luôn đúng một đuôi Model) */
    function modelClassName(name) {
        return (toPascal(name).replace(/Model$/, '') || 'Prop') + 'Model';
    }
    function textureFile(id) {
        const [ns, path] = id.includes(':') ? id.split(':') : ['minecraft', id];
        return [ns, path.startsWith('textures/') ? path : `textures/${path}.png`];
    }
    const snake = n => (String(n || 'part').replace(/[^A-Za-z0-9_]+/g, '_').replace(/^_+|_+$/g, '').toLowerCase() || 'part');
    const camel = n => snake(n).replace(/_([a-z0-9])/g, (m, c) => c.toUpperCase());

    /**
     * tree: [{type:'group', name, origin, rotation, children:[...]} | {type:'cube', name, from, to, inflate, rotation, uv_offset, mirror_uv, box_uv}]
     * opts: {className, pkg, modId, base, entityClass, renderType, texture, texWidth, texHeight, shift, boxUvProject}
     */
    function buildVanillaModelJava(tree, opts) {
        const errors = [], warnings = [];
        const S = opts.shift || 0;
        const P = v => [v[0] + S, v[1], v[2] + S];          // đưa về hệ lưới căn giữa
        const usedFields = new Set(['root']), usedVars = new Set(['root']);
        const uniq = (set, n) => { let k = n, i = 2; while (set.has(k) || JAVA_KEYWORDS.has(k)) k = n + i++; set.add(k); return k; };
        let needDeform = false, cubeCount = 0;
        const fields = [], getters = [], layer = [];
        if (!opts.boxUvProject) warnings.push('Project đang dùng UV từng mặt: lớp vanilla chỉ có box UV, nên lấy texOffs từ "UV offset" của từng hộp. Hình vẫn đúng, chỉ vị trí vân texture có thể khác.');

        function cubeLines(cubes, pivot) {
            return cubes.map(c => {
                cubeCount++;
                if ((c.rotation || [0, 0, 0]).some(x => x)) errors.push(`Hộp "${c.name}" tự xoay: ModelPart không xoay từng hộp. Hãy cho hộp vào một group riêng rồi xoay group.`);
                const f = P(c.from), t = P(c.to);
                const w = r4(t[0] - f[0]), h = r4(t[1] - f[1]), d = r4(t[2] - f[2]);
                const [u, v] = c.uv_offset || [0, 0];
                const needW = u + 2 * (Math.ceil(w) + Math.ceil(d)), needH = v + Math.ceil(d) + Math.ceil(h);
                if (needW > opts.texWidth || needH > opts.texHeight)
                    warnings.push(`Hộp "${c.name}" cần vùng box UV ${needW}x${needH} nhưng texture là ${opts.texWidth}x${opts.texHeight}: UV vượt mép sẽ lặp lại texture (ổn với texture lặp liền như stone).`);
                const args = [pivot[0] - t[0], pivot[1] - t[1], f[2] - pivot[2], w, h, d].map(ff);
                let line = `.texOffs(${u}, ${v})`;
                if (c.mirror_uv) line += '.mirror()';
                if (c.inflate) { needDeform = true; args.push(`new CubeDeformation(${ff(c.inflate)})`); }
                line += `.addBox(${args.join(', ')})`;
                if (c.mirror_uv) line += '.mirror(false)';
                return `${line} // ${c.name}`;
            });
        }
        function builder(lines, indent) {
            if (!lines.length) return 'CubeListBuilder.create()';
            return 'CubeListBuilder.create()\n' + lines.map(l => `${indent}        ${l}`).join('\n').replace(/ \/\/ ([^\n]*)$/, ' /* $1 */');
        }
        // root: pivot (0,0,0) của Blockbench = mặt đất ở tâm entity
        const ROOT = [0, 0, 0];
        function walk(nodes, parentVar, parentPivot, parentField, siblings) {
            const groups = nodes.filter(n => n.type === 'group');
            for (const g of groups) {
                const o = P(g.origin || [0, 0, 0]);
                const partName = uniq(siblings, snake(g.name));
                const field = uniq(usedFields, camel(g.name));
                const hasChildGroups = (g.children || []).some(n => n.type === 'group');
                const varName = hasChildGroups ? uniq(usedVars, field) : null;
                const off = [parentPivot[0] - o[0], parentPivot[1] - o[1], o[2] - parentPivot[2]].map(ff);
                const rot = g.rotation || [0, 0, 0];
                const pose = rot.some(x => x)
                    ? `PartPose.offsetAndRotation(${off.join(', ')}, ${rad(-rot[0])}, ${rad(-rot[1])}, ${rad(rot[2])})`
                    : `PartPose.offset(${off.join(', ')})`;
                const cubes = cubeLines((g.children || []).filter(n => n.type === 'cube'), o);
                layer.push(`        ${varName ? `PartDefinition ${varName} = ` : ''}${parentVar}.addOrReplaceChild("${partName}", ${builder(cubes, '        ')}, ${pose});`);
                fields.push(`    private final ModelPart ${field};`);
                getters.push(`        this.${field} = this.${parentField}.getChild("${partName}");`);
                walk(g.children || [], varName || parentVar, o, field, new Set());
                if (!varName && hasChildGroups) errors.push('lỗi nội bộ: thiếu biến cho group ' + g.name);
            }
        }
        const rootCubes = cubeLines(tree.filter(n => n.type === 'cube'), ROOT);
        layer.push(`        PartDefinition root = parts.addOrReplaceChild("root", ${builder(rootCubes, '        ')}, PartPose.offset(0.0F, 24.0F, 0.0F));`);
        walk(tree, 'root', ROOT, 'root', new Set());
        if (!cubeCount) errors.push('Model không có hộp nào để xuất.');

        const base = BASES[opts.base] ? opts.base : 'HierarchicalModel';
        const rt = RENDER_TYPES[opts.renderType] ? opts.renderType : 'entityCutoutNoCull';
        const ent = (opts.entityClass || '').trim();
        const entSimple = ent ? ent.split('.').pop() : 'T';
        const generic = base !== 'Model' && !ent ? '<T extends Entity>' : '';
        const superType = base === 'Model' ? 'Model' : `${base}<${entSimple}>`;
        const cls = opts.className;
        const [ns, file] = textureFile(opts.texture);

        const imports = new Set(['net.minecraft.client.model.geom.ModelLayerLocation', 'net.minecraft.client.model.geom.ModelPart',
            'net.minecraft.client.model.geom.PartPose', 'net.minecraft.client.model.geom.builders.CubeListBuilder',
            'net.minecraft.client.model.geom.builders.LayerDefinition', 'net.minecraft.client.model.geom.builders.MeshDefinition',
            'net.minecraft.client.model.geom.builders.PartDefinition', 'net.minecraft.client.renderer.RenderType',
            'net.minecraft.resources.ResourceLocation', `net.minecraft.client.model.${base}`]);
        if (needDeform) imports.add('net.minecraft.client.model.geom.builders.CubeDeformation');
        if (base !== 'Model') imports.add(ent || 'net.minecraft.world.entity.Entity');
        if (base !== 'HierarchicalModel') { imports.add('com.mojang.blaze3d.vertex.PoseStack'); imports.add('com.mojang.blaze3d.vertex.VertexConsumer'); }

        const setupAnim = base === 'Model' ? '' : `
    @Override
    public void setupAnim(${entSimple} entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
${base === 'HierarchicalModel' ? '        this.root().getAllParts().forEach(ModelPart::resetPose);   // đứng yên: về tư thế gốc' : '        // đứng yên: không có chuyển động'}
    }
`;
        const tail = base === 'HierarchicalModel' ? `
    @Override
    public ModelPart root() {
        return this.root;
    }
` : `
    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(pose, buffer, light, overlay, red, green, blue, alpha);
    }
`;
        const g2 = generic ? '<>' : '';
        const code = `package ${opts.pkg};

${[...imports].sort().map(i => `import ${i};`).join('\n')}

/**
 * Model "${cls}" sinh bởi plugin Blockbench java_single_texture, theo mẫu vanilla (như FrogModel).
 * Đừng sửa tay: sửa trong Blockbench rồi xuất lại.
 *
 * Đăng ký layer (ModEntityRenderersRegistry, sự kiện RegisterLayerDefinitions):
 *   event.registerLayerDefinition(${cls}.LAYER_LOCATION, ${cls}::createBodyLayer);
 *
 * Trong renderer (EntityRenderer thường, ví dụ PropRenderer):
 *   model = new ${cls}${g2}(ctx.bakeLayer(${cls}.LAYER_LOCATION));
 *   pose.pushPose();
 *   pose.scale(-1F, -1F, 1F);          // model entity có trục Y hướng xuống -> lật lại
 *   pose.translate(0F, -1.501F, 0F);   // root ở y = 24 rơi đúng mặt đất
 *   model.renderToBuffer(pose, buffers.getBuffer(model.renderType(${cls}.TEXTURE)),
 *           light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
 *   pose.popPose();
 */
public class ${cls}${generic} extends ${superType} {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("${opts.modId}", "${snake(cls.replace(/Model$/, ''))}"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("${ns}", "${file}");

    private final ModelPart root;
${fields.join('\n')}

    public ${cls}(ModelPart part) {
        super(RenderType::${rt});
        this.root = part.getChild("root");
${getters.join('\n')}
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

${layer.join('\n')}

        return LayerDefinition.create(mesh, ${opts.texWidth}, ${opts.texHeight});
    }
${setupAnim}${tail}}
`.replace(/\n{3,}/g, '\n\n');
        return {code, errors, warnings, cubeCount, partCount: fields.length + 1};
    }

    function collectTree() {
        const conv = n => {
            if (n instanceof Group) {
                if (n.export === false) return null;
                return {type: 'group', name: n.name, origin: n.origin.slice(), rotation: n.rotation.slice(),
                        children: n.children.map(conv).filter(Boolean)};
            }
            if (n instanceof Cube) {
                if (n.export === false) return null;
                return {type: 'cube', name: n.name, from: n.from.slice(), to: n.to.slice(), inflate: n.inflate,
                        rotation: n.rotation.slice(), uv_offset: (n.uv_offset || [0, 0]).slice(), mirror_uv: !!n.mirror_uv,
                        box_uv: n.box_uv};
            }
            return null;   // mesh, locator… không hỗ trợ
        };
        return Outliner.root.map(conv).filter(Boolean);
    }

    function runEntityExport(form) {
        if (!Cube.all.length) { Blockbench.showQuickMessage('Model chưa có khối nào', 2000); return; }
        const className = modelClassName((form.name || '').trim() || Project.name);
        const ent = (form.entity_class || '').trim();
        if (ent && !/^([a-z_][a-z0-9_]*\.)+[A-Z][A-Za-z0-9_]*$/.test(ent)) {
            Blockbench.showMessageBox({title: 'Lớp entity không hợp lệ', message: `"${ent}" phải là tên đầy đủ, ví dụ net.susu609.the8dungeon.world.prop.PropEntity, hoặc để trống.`});
            return;
        }
        const boxUv = Cube.all.every(c => c.box_uv !== undefined ? c.box_uv : Project.box_uv);
        const opts = {
            className,
            pkg: (form.package || '').trim() || 'net.susu609.the8dungeon.client.model',
            modId: (form.mod_id || '').trim() || 'susu609',
            base: form.base || 'HierarchicalModel',
            entityClass: form.base === 'Model' ? '' : ent,
            renderType: form.render_type || 'entityCutoutNoCull',
            texture: (form.texture || '').trim() || 'minecraft:block/stone',
            texWidth: Project.texture_width || 16,
            texHeight: Project.texture_height || 16,
            shift: Format.centered_grid ? 0 : -8,
            boxUvProject: boxUv
        };
        const {code, errors, warnings, cubeCount, partCount} = buildVanillaModelJava(collectTree(), opts);
        if (errors.length) {
            Blockbench.showMessageBox({title: 'Không xuất được lớp model', message: errors.map(e => '• ' + e).join('\n')});
            return;
        }
        Blockbench.export({type: 'Java Class', extensions: ['java'], name: className, content: code}, () => {
            Blockbench.showQuickMessage(`Đã xuất ${className}.java (${partCount} part, ${cubeCount} hộp)`, 2500);
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
                    title: 'Export Java Entity Model (Vanilla Box UV)',
                    form: {
                        name: {label: 'Tên (tự thêm đuôi Model)', type: 'text', value: ''},
                        base: {label: 'Thừa kế lớp', type: 'select', options: BASES, value: 'HierarchicalModel'},
                        entity_class: {label: 'Lớp entity (để trống = generic T extends Entity)', type: 'text', value: '',
                            condition: f => f.base !== 'Model'},
                        render_type: {label: 'RenderType', type: 'select', options: RENDER_TYPES, value: 'entityCutoutNoCull'},
                        mod_id: {label: 'Mod ID (cho LAYER_LOCATION)', type: 'text', value: 'susu609'},
                        package: {label: 'Package', type: 'text', value: 'net.susu609.the8dungeon.client.model'},
                        texture: {label: 'Texture', type: 'text', value: 'minecraft:block/stone'}
                    },
                    onOpen() {
                        const n = modelClassName(Project && Project.name).replace(/Model$/, '');
                        if (this.setFormValues) this.setFormValues({name: n});
                    },
                    onConfirm(form) { runEntityExport(form); }
                });
                entityAction = new Action('export_java_single_texture_entity', {
                    name: 'Export Java Entity Model (Vanilla Box UV)',
                    description: 'Lớp model kiểu vanilla: createBodyLayer, texOffs/addBox, PartPose',
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

    if (typeof module !== 'undefined') module.exports = {buildJavaModel, buildVanillaModelJava, modelClassName};
})();
