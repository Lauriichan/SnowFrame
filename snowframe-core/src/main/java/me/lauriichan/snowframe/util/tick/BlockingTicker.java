package me.lauriichan.snowframe.util.tick;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.longs.LongConsumer;

public final class BlockingTicker {

    private final LongConsumer executable;

    private final AtomicInteger state = new AtomicInteger(0);

    private volatile Thread currentThread;

    private final TimeSync sync = new TimeSync();

    public final TimeSync sync() {
        return sync;
    }

    public BlockingTicker(LongConsumer executable) {
        this.executable = Objects.requireNonNull(executable);
    }

    public final Thread currentThread() {
        return currentThread;
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
            int currentState;
            while (true) {
                if ((currentState = state.get()) == 0) {
                    break;
                }
                if (currentState == 2) {
                    sync.pauseTick();
                    continue;
                }
                try {
                    executable.accept(sync.newTick());
                } finally {
                    sync.endTick();
                }
            }
        } finally {
            currentThread = null;
        }
    }

}
