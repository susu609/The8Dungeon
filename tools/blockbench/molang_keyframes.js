/**
 * Blockbench plugin: Molang Keyframes
 *
 * Đổi công thức Molang (ví dụ math.sin(query.anim_time * 7200 / 2) * 0.2) thành các KEYFRAME SỐ ĐỘC LẬP
 * ngay trong animation đang sửa, để nút xuất Java AnimationDefinition có sẵn của Blockbench ghi được.
 *
 * Hai nút (menu Animation, ở tab Animate):
 *  1. "Expand Molang Keyframes": tách các keyframe Molang ĐANG CHỌN thành keyframe số, lấy mẫu mỗi 1/fps giây
 *     từ keyframe đó tới keyframe kế tiếp (hoặc hết animation). Keyframe gốc bị thay thế. Có Undo.
 *  2. "Molang → Keyframes": gõ công thức cho X/Y/Z, chọn kênh, thời điểm, độ dài, fps -> chèn chuỗi keyframe
 *     số vào group đang chọn của animation đang mở.
 *
 * Cài: File -> Plugins -> Load Plugin from File -> chọn file này.
 */
(function () {
    'use strict';

    const CHANNELS = ['position', 'rotation', 'scale'];
    const DEG = Math.PI / 180;
    const r4 = v => Math.round(v * 10000) / 10000 + 0;

    // ---------- Molang -> hàm JS (an toàn: chỉ cho phép tên trong danh sách) ----------
    const FN = {
        'math.sin': 'M.sin', 'math.cos': 'M.cos', 'math.asin': 'M.asin', 'math.acos': 'M.acos',
        'math.atan': 'M.atan', 'math.atan2': 'M.atan2', 'math.abs': 'Math.abs', 'math.floor': 'Math.floor',
        'math.ceil': 'Math.ceil', 'math.round': 'Math.round', 'math.trunc': 'Math.trunc', 'math.sqrt': 'Math.sqrt',
        'math.pow': 'Math.pow', 'math.exp': 'Math.exp', 'math.ln': 'Math.log', 'math.min': 'Math.min',
        'math.max': 'Math.max', 'math.clamp': 'M.clamp', 'math.lerp': 'M.lerp', 'math.mod': 'M.mod',
        'math.pi': 'Math.PI', 'query.anim_time': 't', 'q.anim_time': 't'
    };
    const M = {
        sin: d => Math.sin(d * DEG), cos: d => Math.cos(d * DEG),
        asin: x => Math.asin(x) / DEG, acos: x => Math.acos(x) / DEG, atan: x => Math.atan(x) / DEG,
        atan2: (y, x) => Math.atan2(y, x) / DEG,
        clamp: (v, a, b) => Math.min(Math.max(v, a), b), lerp: (a, b, k) => a + (b - a) * k,
        mod: (a, b) => a % b
    };
    const cache = new Map();

    /** Biên dịch một ô keyframe (số hoặc Molang) thành f(t). Ném lỗi nếu có tên lạ. */
    function compile(src) {
        const key = String(src);
        if (cache.has(key)) return cache.get(key);
        let s = key.trim().toLowerCase();
        if (s === '') s = '0';
        if (/^-?\d+(\.\d+)?(e-?\d+)?$/.test(s)) { const n = Number(s); const f = () => n; f.constant = true; cache.set(key, f); return f; }
        if (/math\.random/.test(s)) throw new Error(`"${key}": math.random không nướng được (mỗi lần ra số khác)`);
        if (/[^a-z0-9_.+\-*/%()<>=!&|?:,\s]/.test(s)) throw new Error(`"${key}": có ký tự không hỗ trợ`);
        const js = s.replace(/[a-z_][a-z0-9_]*(\.[a-z_][a-z0-9_]*)*/g, id => {
            if (FN[id]) return FN[id];
            throw new Error(`"${key}": không hỗ trợ "${id}"`);
        });
        let f;
        try { f = new Function('t', 'M', `return (${js});`); } catch (e) { throw new Error(`"${key}": sai cú pháp`); }
        const g = t => { const v = f(t, M); if (typeof v !== 'number' || !isFinite(v)) throw new Error(`"${key}": ra giá trị không phải số tại t = ${t}`); return v; };
        g.constant = false; cache.set(key, g); return g;
    }

    /**
     * Giá trị của một kênh tại thời điểm t (giống cách Blockbench nội suy: tính công thức của 2 keyframe
     * kề nhau tại t rồi nội suy tuyến tính; 'step' giữ keyframe trước).
     * kfs: [{time, x, y, z, interpolation}] đã sắp theo time.
     */
    function sampleAt(kfs, t) {
        const val = (k) => ['x', 'y', 'z'].map(a => compile(k[a])(t));
        if (t <= kfs[0].time) return val(kfs[0]);
        const last = kfs[kfs.length - 1];
        if (t >= last.time) return val(last);
        let i = 0; while (kfs[i + 1].time < t) i++;
        const a = kfs[i], b = kfs[i + 1];
        if (a.interpolation === 'step') return val(a);
        const k = (t - a.time) / (b.time - a.time), va = val(a), vb = val(b);
        return va.map((v, j) => v + (vb[j] - v) * k);
    }


    /** Danh sách mốc thời gian từ t0 tới t1, bước 1/fps. includeEnd: có lấy cả t1 không. */
    function times(t0, t1, fps, includeEnd) {
        const out = [], n = Math.round((t1 - t0) * fps);
        for (let i = 0; i <= n; i++) {
            const t = r4(Math.min(t0 + i / fps, t1));
            if (!includeEnd && t >= t1 - 1e-9) break;
            if (!out.length || t > out[out.length - 1]) out.push(t);
        }
        return out;
    }

    /** Cảnh báo khi lấy mẫu quá thưa so với dao động (ví dụ chu kỳ 2 tick mà lấy 20 fps chỉ trúng điểm 0). */
    function aliasWarnings(evalAt, t0, t1, keys, label) {
        const w = [], peak = [0, 0, 0], got = [0, 0, 0], fine = 480;
        for (let i = 0; i <= fine; i++) evalAt(t0 + (t1 - t0) * i / fine).forEach((v, j) => peak[j] = Math.max(peak[j], Math.abs(v)));
        keys.forEach(k => ['x', 'y', 'z'].forEach((a, j) => got[j] = Math.max(got[j], Math.abs(k[a]))));
        ['x', 'y', 'z'].forEach((a, j) => {
            if (peak[j] > 1e-6 && got[j] < peak[j] * 0.5)
                w.push(`${label}.${a}: fps quá thưa so với tốc độ dao động (đỉnh thật ${r4(peak[j])}, keyframe chỉ đạt ${r4(got[j])}). Tăng fps hoặc đổi sin thành cos.`);
        });
        return w;
    }

    /**
     * Tách các keyframe Molang của MỘT kênh. kfs: [{time, x, y, z, interpolation, id}]; chosen: tập id cần tách
     * (bỏ trống = mọi keyframe có Molang). Trả về {remove: [id], add: [{time,x,y,z}], warnings}.
     * Giá trị lấy đúng như Blockbench xem trước (sampleAt trên cả kênh GỐC), nên đoạn trước/sau không đổi.
     */
    function expandChannel(kfsIn, length, fps, chosen, label) {
        const kfs = kfsIn.slice().sort((a, b) => a.time - b.time);
        const remove = [], add = [], warnings = [];
        kfs.forEach((k, i) => {
            const molang = ['x', 'y', 'z'].some(a => !compile(k[a]).constant);
            if (!molang || (chosen && !chosen.has(k.id))) return;
            const next = kfs[i + 1];
            const t0 = k.time, t1 = next ? next.time : Math.max(length, t0);
            if (t1 <= t0) { warnings.push(`${label} tại ${t0}s: không có khoảng thời gian phía sau để tách (đặt độ dài animation lớn hơn).`); return; }
            if (k.interpolation === 'catmullrom' || k.interpolation === 'bezier') warnings.push(`${label} tại ${t0}s: nội suy catmullrom/bezier được tách như linear.`);
            const keys = times(t0, t1, fps, !next).map(t => { const [x, y, z] = sampleAt(kfs, t); return {time: t, x: r4(x), y: r4(y), z: r4(z)}; });
            warnings.push(...aliasWarnings(t => sampleAt(kfs, t), t0, t1, keys, `${label} tại ${t0}s`));
            remove.push(k.id); add.push(...keys);
        });
        return {remove, add, warnings};
    }

    /** Sinh keyframe từ công thức gõ tay. exprs: {x, y, z} (chuỗi Molang hoặc số). */
    function keyframesFromExpr(exprs, start, duration, fps, label) {
        const f = ['x', 'y', 'z'].map(a => compile(exprs[a] === undefined ? '0' : exprs[a]));
        const evalAt = t => f.map(g => g(t - start));   // query.anim_time tính từ thời điểm bắt đầu chèn
        const keys = times(start, start + duration, fps, true).map(t => { const [x, y, z] = evalAt(t); return {time: t, x: r4(x), y: r4(y), z: r4(z)}; });
        return {keys, warnings: aliasWarnings(evalAt, start, start + duration, keys, label)};
    }

    // ---------- Gắn vào Blockbench ----------
    const dpOf = kf => (kf.data_points && kf.data_points[0]) || {};
    function addKeys(animator, channel, keys) {
        for (const k of keys) animator.addKeyframe({channel, time: k.time, interpolation: 'linear', data_points: [{x: k.x, y: k.y, z: k.z}]});
    }

    function runExpand(fps) {
        const anim = Animation.selected;
        const selected = (Timeline.selected || []).slice();
        if (!anim) return {lines: ['Hãy mở một animation trước.']};
        const chosen = selected.length ? new Set(selected.map(k => k.uuid)) : null;
        const lines = [], warnings = [];
        Undo.initEdit({animations: [anim]});
        let total = 0;
        for (const animator of Object.values(anim.animators || {})) {
            for (const ch of CHANNELS) {
                const list = animator[ch] || [];
                if (!list.length) continue;
                const kfs = list.map(kf => ({id: kf.uuid, time: kf.time, interpolation: kf.interpolation, ...(({x, y, z}) => ({x, y, z}))(dpOf(kf))}));
                const r = expandChannel(kfs, anim.length, fps, chosen, `${animator.name}.${ch}`);
                if (!r.remove.length) continue;
                list.filter(kf => r.remove.includes(kf.uuid)).forEach(kf => kf.remove());
                addKeys(animator, ch, r.add);
                total += r.remove.length; warnings.push(...r.warnings);
                lines.push(`• ${animator.name}.${ch}: ${r.remove.length} keyframe Molang → ${r.add.length} keyframe số`);
            }
        }
        Undo.finishEdit('Expand Molang keyframes', {animations: [anim]});
        if (!total) lines.push(chosen ? 'Các keyframe đang chọn không có công thức Molang.' : 'Animation này không có keyframe Molang nào.');
        return {lines, warnings};
    }

    function runInsert(form) {
        const anim = Animation.selected;
        const group = (typeof Group !== 'undefined') && (Group.first_selected || Group.selected);
        if (!anim || !group) return {lines: ['Hãy mở một animation và chọn một group (bone) trước.']};
        const fps = Math.max(1, Math.round(form.fps));
        const start = Math.max(0, +form.start || 0);
        const duration = +form.duration > 0 ? +form.duration : Math.max(anim.length - start, 1 / fps);
        const r = keyframesFromExpr({x: form.x, y: form.y, z: form.z}, start, duration, fps, `${group.name}.${form.channel}`);
        Undo.initEdit({animations: [anim]});
        const animator = anim.getBoneAnimator(group);
        if (form.clear) (animator[form.channel] || []).filter(kf => kf.time >= start - 1e-9 && kf.time <= start + duration + 1e-9).forEach(kf => kf.remove());
        addKeys(animator, form.channel, r.keys);
        if (anim.length < start + duration) anim.setLength ? anim.setLength(start + duration) : (anim.length = start + duration);
        Undo.finishEdit('Molang to keyframes', {animations: [anim]});
        return {lines: [`Đã chèn ${r.keys.length} keyframe ${form.channel} vào "${group.name}" (${start}s → ${r4(start + duration)}s, ${fps} fps).`], warnings: r.warnings};
    }

    function report(title, res) {
        const lines = res.lines.slice();
        if (res.warnings && res.warnings.length) lines.push('', 'Lưu ý:', ...res.warnings.map(w => '• ' + w));
        Blockbench.showMessageBox({title, message: lines.join('\n')});
    }

    let expandAction, insertAction, expandDialog, insertDialog;
    if (typeof BBPlugin !== 'undefined') {
        BBPlugin.register('molang_keyframes', {
            title: 'Molang Keyframes',
            author: 'The8Dungeon',
            icon: 'functions',
            description: 'Đổi công thức Molang thành keyframe số độc lập để xuất Java AnimationDefinition.',
            version: '1.0.0',
            variant: 'both',
            onload() {
                expandDialog = new Dialog({
                    id: 'molang_expand_dialog', title: 'Expand Molang Keyframes',
                    lines: ['Tách các keyframe Molang đang chọn (không chọn = mọi keyframe Molang của animation).'],
                    form: {fps: {label: 'Lấy mẫu (fps) — 20 = mỗi tick', type: 'number', value: 20, min: 1, max: 240}},
                    onConfirm(form) {
                        try { report('Expand Molang Keyframes', runExpand(Math.max(1, Math.round(form.fps)))); }
                        catch (e) { Blockbench.showMessageBox({title: 'Không tách được', message: String(e.message || e)}); }
                    }
                });
                insertDialog = new Dialog({
                    id: 'molang_insert_dialog', title: 'Molang → Keyframes',
                    form: {
                        channel: {label: 'Kênh', type: 'select', value: 'position', options: {position: 'Position (px)', rotation: 'Rotation (độ)', scale: 'Scale'}},
                        x: {label: 'X', type: 'text', value: 'math.sin(query.anim_time * 7200 / 2) * 0.2'},
                        y: {label: 'Y', type: 'text', value: '0'},
                        z: {label: 'Z', type: 'text', value: '0'},
                        start: {label: 'Bắt đầu (giây)', type: 'number', value: 0, min: 0, step: 0.05},
                        duration: {label: 'Độ dài (giây, 0 = tới hết animation)', type: 'number', value: 0, min: 0, step: 0.05},
                        fps: {label: 'Lấy mẫu (fps)', type: 'number', value: 40, min: 1, max: 240},
                        clear: {label: 'Xoá keyframe cũ của kênh trong khoảng này', type: 'checkbox', value: true}
                    },
                    onConfirm(form) {
                        try { report('Molang → Keyframes', runInsert(form)); }
                        catch (e) { Blockbench.showMessageBox({title: 'Không chèn được', message: String(e.message || e)}); }
                    }
                });
                expandAction = new Action('expand_molang_keyframes', {
                    name: 'Expand Molang Keyframes', icon: 'functions',
                    description: 'Tách keyframe có công thức Molang thành keyframe số',
                    condition: () => typeof Modes === 'undefined' || Modes.animate,
                    click() { expandDialog.show(); }
                });
                insertAction = new Action('molang_to_keyframes', {
                    name: 'Molang → Keyframes', icon: 'functions',
                    description: 'Gõ công thức Molang, chèn chuỗi keyframe số vào group đang chọn',
                    condition: () => typeof Modes === 'undefined' || Modes.animate,
                    click() { insertDialog.show(); }
                });
                MenuBar.addAction(expandAction, 'animation');
                MenuBar.addAction(insertAction, 'animation');
            },
            onunload() {
                [expandAction, insertAction].forEach(a => a && a.delete());
                [expandDialog, insertDialog].forEach(d => d && d.delete && d.delete());
            }
        });
    }

    if (typeof module !== 'undefined') module.exports = {compile, sampleAt, expandChannel, keyframesFromExpr};
})();
