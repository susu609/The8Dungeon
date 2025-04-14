package net.ss.sudungeon.client.gui.overlay;

public class OverlayStateManager {
    private int fadeInDuration;
    private int holdDuration;
    private int fadeOutDuration;
    private int currentTick;

    public OverlayStateManager (int fadeIn, int hold, int fadeOut) {
        this.fadeInDuration = fadeIn;
        this.holdDuration = hold;
        this.fadeOutDuration = fadeOut;
        this.currentTick = 0;
    }

    public void reset () {
        this.currentTick = 0;
    }

    public void update () {
        this.currentTick++;
    }

    public boolean isFadingIn () {
        return currentTick <= fadeInDuration;
    }

    public boolean isHolding () {
        return currentTick > fadeInDuration && currentTick <= fadeInDuration + holdDuration;
    }

    public boolean isFadingOut () {
        return currentTick > fadeInDuration + holdDuration && currentTick <= fadeInDuration + holdDuration + fadeOutDuration;
    }

    public boolean isFinished () {
        return currentTick > fadeInDuration + holdDuration + fadeOutDuration;
    }

    public float calculateAlpha () {
        if (isFadingIn()) {
            return (float) currentTick / fadeInDuration;
        } else if (isHolding()) {
            return 1.0F;
        } else if (isFadingOut()) {
            return 1.0F - ((float) (currentTick - fadeInDuration - holdDuration) / fadeOutDuration);
        }
        return 0.0F;
    }
}