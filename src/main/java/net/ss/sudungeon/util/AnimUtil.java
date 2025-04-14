package net.ss.sudungeon.util;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.ss.sudungeon.world.entity.ModAnimationState;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

public class AnimUtil {
    private static final Map<AnimationState, Long> startedTicks = new WeakHashMap<>();
    private static final Vector3f TEMP_VEC = new Vector3f();

    public static void animate(HierarchicalModel<?> model, ModAnimationState state, AnimationDefinition def, long tick, float partialTick) {
        if (!state.isRunning()) return;

        float time = state.getTime(tick, partialTick);
        float length = def.lengthInSeconds() * 20f;

        if (!state.isLooping() && time > length) {
            state.stop();
            return;
        }

        KeyframeAnimations.animate(model, def, tick, partialTick, TEMP_VEC);
    }



    public static void playOnce(Player player, AnimationState state, long ageInTicks) {
        state.start((int) ageInTicks);
        startedTicks.put(state, ageInTicks);
    }

    public static void loop(Player player, AnimationState state, long ageInTicks) {
        if (!state.isStarted()) {
            state.start((int) ageInTicks);
            startedTicks.put(state, ageInTicks);
        }
    }

    public static void stopIfRunning(AnimationState state) {
        if (state.isStarted()) {
            state.stop();
            startedTicks.remove(state);
        }
    }

    public static void playOnceWithAutoStop(Player player, AnimationState state, long ageInTicks, int durationTicks) {
        if (!state.isStarted()) {
            state.start((int) ageInTicks);
            startedTicks.put(state, ageInTicks);
        }

        long startTick = startedTicks.getOrDefault(state, -1L);
        if (startTick >= 0 && ageInTicks - startTick >= durationTicks) {
            state.stop();
            startedTicks.remove(state);
        }
    }
}

