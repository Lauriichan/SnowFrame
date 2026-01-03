package me.lauriichan.snowframe.util.tick;

import java.util.concurrent.TimeUnit;

public final class TimeSync {

    private static final long MILLI_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final long SEC_IN_NANOS = TimeUnit.SECONDS.toNanos(1);
    private static final long MIN_IN_NANOS = TimeUnit.MINUTES.toNanos(1);

    private volatile long length = MILLI_IN_NANOS * 50, pauseLength = MILLI_IN_NANOS * 250;

    private volatile int tpsCounter = 0, tpmCounter = 0;
    private volatile int tps = -1, tpm = -1;

    private long nextLength;
    private long time, prevTime;
    private long elapsedSecond = 0, elapsedMinute = 0;
    private long delta = 0;

    private int cycles;

    public TimeSync() {
        prevTime = time = System.nanoTime();
    }

    /*
     * Sync
     */

    public final long newTick() {
        prevTime = time;
        time = System.nanoTime();
        delta = time - prevTime;
        elapsedSecond += delta;
        elapsedMinute += delta;
        if (elapsedSecond >= SEC_IN_NANOS) {
            elapsedSecond -= SEC_IN_NANOS;
            this.tps = tpsCounter;
            tpmCounter += tpsCounter;
            tpsCounter = 0;
        }
        if (elapsedMinute >= MIN_IN_NANOS) {
            elapsedMinute -= MIN_IN_NANOS;
            this.tpm = tpmCounter + tpsCounter;
            tpmCounter = 0;
        }
        return delta;
    }

    public final void endTick() {
        tpsCounter++;
        spin(length);
    }

    public final void pauseTick() {
        spin(pauseLength);
    }

    private final void spin(long length) {
        while (true) {
            nextLength = length - (System.nanoTime() - time);
            cycles = (int) (nextLength / 1000);
            if (cycles <= 0) {
                break;
            }
            while (cycles-- > 0) {
                Thread.yield();
            }
        }
    }

    /*
     * Config
     */

    public final void pauseLength(final long pauseLength, final TimeUnit unit) {
        this.pauseLength = Math.max(unit.toNanos(pauseLength), MILLI_IN_NANOS * 10L);
    }

    public final void pauseLength(final long lengthInNanos) {
        this.pauseLength = Math.max(lengthInNanos, MILLI_IN_NANOS * 10L);
    }

    public final long pauseLength() {
        return pauseLength;
    }

    public final void length(final long length, final TimeUnit unit) {
        this.length = Math.max(unit.toNanos(length), MILLI_IN_NANOS);
    }

    public final void length(final long lengthInNanos) {
        this.length = Math.max(lengthInNanos, MILLI_IN_NANOS);
    }

    public final long length() {
        return length;
    }

    /*
     * Getter
     */

    public final int tps() {
        if (tps == -1) {
            return tpsCounter;
        }
        return tps;
    }

    public final int tpm() {
        if (tpm == -1) {
            return tpsCounter + tpmCounter;
        }
        return tpm;
    }

}
