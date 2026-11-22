/**
 * Blockbench plugin: Molang Bake
 *
 * Nút xuất Java animation (AnimationDefinition) của Blockbench chỉ ghi được keyframe SỐ.
 * Plugin này "nướng" keyframe có công thức Molang (ví dụ math.sin(query.anim_time * 7200 / 2) * 0.2)
 * thành chuỗi keyframe số LINEAR, lấy mẫu ở một fps cố định, vào một animation MỚI (…_baked).
 * Sau đó dùng nút xuất animation Java có sẵn của Blockbench như bình thường.
 *
 * Cài: File -> Plugins -> Load Plugin from File -> chọn file này.
 * Dùng: tab Animate -> chọn animation -> Animation -> "Bake Molang Keyframes".
 *
 * Molang hỗ trợ: số, + - * / %, ngoặc, so sánh, ?:, && ||, query.anim_time (q.anim_time),
 * math.sin/cos/asin/acos/atan/atan2 (ĐỘ), abs, floor, ceil, round, trunc, sqrt, pow, exp, ln,
 * min, max, clamp, lerp, mod, pi. math.random bị từ chối (mỗi lần nướng sẽ ra khác nhau).
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

    const hasMolang = kfs => kfs.some(k => ['x', 'y', 'z'].some(a => !compile(k[a]).constant));

    /**
     * Nướng một kênh: trả về danh sách keyframe số {time, x, y, z} và cảnh báo.
     * Kênh không có Molang thì giữ nguyên keyframe gốc (không lấy mẫu).
     */
    function bakeChannel(kfsIn, length, fps, label) {
        const warnings = [];
        const kfs = kfsIn.slice().sort((a, b) => a.time - b.time);
        if (!kfs.length) return {keys: [], baked: false, warnings};
        if (!hasMolang(kfs)) {
            return {keys: kfs.map(k => ({time: r4(k.time), x: +k.x || 0, y: +k.y || 0, z: +k.z || 0, interpolation: k.interpolation || 'linear'})), baked: false, warnings};
        }
        if (kfs.some(k => k.interpolation === 'catmullrom' || k.interpolation === 'bezier'))
            warnings.push(`${label}: nội suy catmullrom/bezier được nướng như linear.`);
        const end = Math.max(length, kfs[kfs.length - 1].time);
        const n = Math.max(1, Math.round(end * fps));
        const keys = [];
        for (let i = 0; i <= n; i++) {
            const t = r4(i / fps > end ? end : i / fps);
            const [x, y, z] = sampleAt(kfs, t);
            keys.push({time: t, x: r4(x), y: r4(y), z: r4(z), interpolation: 'linear'});
        }
        // Kiểm tra lấy mẫu quá thưa (ví dụ rung 10 lần/giây mà nướng 20 fps chỉ trúng các điểm 0)
        const fine = Math.max(n * 8, 240), peak = [0, 0, 0], got = [0, 0, 0];
        for (let i = 0; i <= fine; i++) { const v = sampleAt(kfs, end * i / fine); v.forEach((a, j) => peak[j] = Math.max(peak[j], Math.abs(a))); }
        keys.forEach(k => ['x', 'y', 'z'].forEach((a, j) => got[j] = Math.max(got[j], Math.abs(k[a]))));
        ['x', 'y', 'z'].forEach((a, j) => {
            if (peak[j] > 1e-6 && got[j] < peak[j] * 0.5)
                warnings.push(`${label}.${a}: ${fps} fps quá thưa so với tốc độ dao động (đỉnh thật ${r4(peak[j])}, sau khi nướng chỉ còn ${r4(got[j])}). Tăng fps hoặc đổi sin thành cos.`);
        });
        return {keys, baked: true, warnings};
    }

    // ---------- Gắn vào Blockbench ----------
    function readChannel(animator, channel) {
        return (animator[channel] || []).map(kf => {
            const dp = (kf.data_points && kf.data_points[0]) || {};
            return {time: kf.time, x: dp.x, y: dp.y, z: dp.z, interpolation: kf.interpolation};
        });
    }

    function bakeAnimation(src, fps, overwrite) {
        const warnings = [], bakedChannels = [];
        const plan = [];
        for (const animator of Object.values(src.animators || {})) {
            for (const ch of CHANNELS) {
                const kfs = readChannel(animator, ch);
                if (!kfs.length) continue;
                const label = `${animator.name}.${ch}`;
                const r = bakeChannel(kfs, src.length, fps, label);
                warnings.push(...r.warnings);
                if (r.baked) bakedChannels.push(label);
                plan.push({animator, ch, keys: r.keys});
            }
        }
        if (!bakedChannels.length) return {warnings, bakedChannels, target: null};

        Undo.initEdit({animations: overwrite ? [src] : []});
        let target = src;
        if (!overwrite) {
            target = new Animation({name: src.name + '_baked', length: src.length, loop: src.loop, snapping: fps}).add(false);
        }
        for (const {animator, ch, keys} of plan) {
            const dest = target.getBoneAnimator(animator.group || animator.getElement && animator.getElement());
            if (overwrite) (dest[ch] || []).slice().forEach(kf => kf.remove());
            for (const k of keys) {
                dest.addKeyframe({channel: ch, time: k.time, interpolation: k.interpolation,
                                  data_points: [{x: k.x, y: k.y, z: k.z}]});
            }
        }
        Undo.finishEdit('Bake Molang keyframes', {animations: [target]});
        target.select();
        return {warnings, bakedChannels, target};
    }

    let action, dialog;
    if (typeof BBPlugin !== 'undefined') {
        BBPlugin.register('molang_bake', {
            title: 'Molang Bake',
            author: 'The8Dungeon',
            icon: 'local_fire_department',
            description: 'Nướng keyframe có công thức Molang thành keyframe số để xuất Java AnimationDefinition.',
            version: '1.0.0',
            variant: 'both',
            onload() {
                dialog = new Dialog({
                    id: 'molang_bake_dialog',
                    title: 'Bake Molang Keyframes',
                    form: {
                        fps: {label: 'Lấy mẫu (fps) — 20 = mỗi tick', type: 'number', value: 20, min: 1, max: 240},
                        overwrite: {label: 'Ghi đè animation gốc (mặc định: tạo bản …_baked)', type: 'checkbox', value: false}
                    },
                    onConfirm(form) {
                        const src = typeof Animation !== 'undefined' && Animation.selected;
                        if (!src) { Blockbench.showQuickMessage('Hãy chọn một animation trước', 2000); return; }
                        try {
                            const r = bakeAnimation(src, Math.max(1, Math.round(form.fps)), !!form.overwrite);
                            const lines = r.target
                                ? [`Đã nướng ${r.bakedChannels.length} kênh vào "${r.target.name}" (${form.fps} fps):`, ...r.bakedChannels.map(c => '• ' + c)]
                                : ['Animation này không có keyframe Molang nào, không cần nướng.'];
                            if (r.warnings.length) lines.push('', 'Lưu ý:', ...r.warnings.map(w => '• ' + w));
                            Blockbench.showMessageBox({title: 'Molang Bake', message: lines.join('\n')});
                        } catch (e) {
                            Blockbench.showMessageBox({title: 'Không nướng được', message: String(e.message || e)});
                        }
                    }
                });
                action = new Action('bake_molang_keyframes', {
                    name: 'Bake Molang Keyframes',
                    description: 'Đổi keyframe Molang thành keyframe số (animation mới …_baked)',
                    icon: 'local_fire_department',
                    condition: () => typeof Modes === 'undefined' || Modes.animate,
                    click() { dialog.show(); }
                });
                MenuBar.addAction(action, 'animation');
            },
            onunload() {
                if (action) action.delete();
                if (dialog && dialog.delete) dialog.delete();
            }
        });
    }

    if (typeof module !== 'undefined') module.exports = {compile, sampleAt, bakeChannel};
})();
