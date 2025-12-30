package me.lauriichan.snowframe.util.tick;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.longs.LongConsumer;

public class BlockingTicker {

    private static final long MIN_IN_NANOS = TimeUnit.MINUTES.toNanos(1);
    private static final long SEC_IN_NANOS = TimeUnit.SECONDS.toNanos(1);
    private static final long MILLI_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private final LongConsumer executable;

    private final AtomicInteger state = new AtomicInteger(0);

    private volatile int tps;
    private volatile int tpm;

    private volatile long length = MILLI_IN_NANOS * 50L;
    private volatile long pauseLength = MILLI_IN_NANOS * 250L;

    private volatile Thread currentThread;

    public BlockingTicker(LongConsumer executable) {
        this.executable = executable;
    }
    
    public final Thread currentThread() {
        return currentThread;
    }

    public final void setPauseLength(final long pauseLength, final TimeUnit unit) {
        this.pauseLength = Math.max(unit.toNanos(pauseLength), MILLI_IN_NANOS * 10L);
    }

    public final long getPauseLength() {
        return pauseLength;
    }

    public final void setLength(final long length, final TimeUnit unit) {
        this.length = Math.max(unit.toNanos(length), MILLI_IN_NANOS);
    }

    public final long getLength() {
        return length;
    }

    public final int getTicksPerMinute() {
        return tpm;
    }

    public final int getTicksPerSecond() {
        return tps;
    }

    public final boolean isAlive() {
        return currentThread != null;
    }

    public final boolean isPaused() {
        return state.get() == 2;
    }
    
    public final void start() {
        if (!state.compareAndSet(0, 1)) {
            state.compareAndSet(2, 1);
        }
    }

    public final void pause() {
        state.compareAndSet(1, 2);
    }
    
    public final void stop() {
        Thread thread = currentThread;
        if (thread == null) {
            return;
        }
        // This will shutdown the thread
        state.set(0);
        thread.interrupt();
    }

    public final void run() {
        if (currentThread != null) {
            return;
        }
        start();
        currentThread = Thread.currentThread();
        try {
            long nextLength = this.length;
            long tickMillis;
            int tickNanos;
            long cycles;
            long prevNanoTime = System.nanoTime();
            long nanoTime = prevNanoTime;
            long delta = 0;
            long elapsed = 0, elapsedMin = 0;
            int counter = 0;
            int secondCounter = 0;
            int currentState;
            while (true) {
                if ((currentState = state.get()) == 0) {
                    break;
                }
                try {
                    if (currentState == 2) {
                        Thread.sleep(this.pauseLength);
                        continue;
                    }
                    prevNanoTime = nanoTime;
                    nanoTime = System.nanoTime();
                    delta = nanoTime - prevNanoTime;
                    elapsed += delta;
                    elapsedMin += delta;
                    executable.accept(delta);
                    if (elapsed >= SEC_IN_NANOS) {
                        elapsed = SEC_IN_NANOS - elapsed;
                        this.tps = counter;
                        secondCounter += counter;
                        counter = 0;
                    }
                    if (elapsedMin >= MIN_IN_NANOS) {
                        elapsedMin = MIN_IN_NANOS - elapsedMin;
                        this.tpm = secondCounter + counter;
                        secondCounter = 0;
                    }
                    counter++;
                    delta = nanoTime - System.nanoTime();
                    nextLength = this.length;
                    tickMillis = TimeUnit.NANOSECONDS.toMillis(nextLength - delta);
                    tickNanos = (int) (nextLength - delta - TimeUnit.MILLISECONDS.toNanos(tickMillis));
                    if (tickMillis > 2) {
                        Thread.sleep(tickMillis, tickNanos);
                        continue;
                    }
                    cycles = (TimeUnit.MILLISECONDS.toNanos(tickMillis) + tickNanos) / 2;
                    while (cycles-- >= 0) {
                        Thread.yield();
                    }
                } catch (final InterruptedException e) {
                    continue;
                }
            }
        } finally {
            currentThread = null;
        }
    }

}
