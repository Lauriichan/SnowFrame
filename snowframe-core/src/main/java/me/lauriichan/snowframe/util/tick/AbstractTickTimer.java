package me.lauriichan.snowframe.util.tick;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTickTimer {

    private final AtomicInteger state = new AtomicInteger(0);

    private volatile Thread timerThread;

    private volatile String name = null;
    private volatile boolean daemon = true;

    private final TimeSync sync = new TimeSync();

    public final TimeSync sync() {
        return sync;
    }

    public final void setName(final String name) {
        this.name = name;
        updateName();
    }

    public final String getName() {
        return name;
    }

    public final boolean hasName() {
        return name != null;
    }

    private final void updateName() {
        Thread thread = this.timerThread;
        String name = this.name;
        if (thread == null || name == null || name.isBlank()) {
            return;
        }
        thread.setName("TickTimer - " + name);
    }

    public final void setDaemon(boolean daemon) {
        if (this.daemon == daemon) {
            return;
        }
        this.daemon = daemon;
        updateDaemon();
    }

    public final boolean isDaemon() {
        return daemon;
    }

    private final void updateDaemon() {
        Thread thread = this.timerThread;
        boolean daemon = this.daemon;
        if (thread == null) {
            return;
        }
        thread.setDaemon(daemon);
    }

    public final boolean isAlive() {
        return timerThread != null;
    }

    public final boolean isPaused() {
        return state.get() == 2;
    }

    public final void pause() {
        state.compareAndSet(1, 2);
    }

    public final void start() {
        if (timerThread != null) {
            state.compareAndSet(2, 1);
            return;
        }
        state.compareAndSet(0, 1);
        timerThread = new Thread(this::tickThread);
        updateName();
        updateDaemon();
        timerThread.start();
    }

    public final void stop() {
        if (timerThread == null) {
            return;
        }
        // This will shutdown the thread
        state.set(0);
        timerThread.interrupt();
        timerThread = null;
    }

    private final void tickThread() {
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
                tick(sync.newTick());
            } finally {
                sync.endTick();
            }
        }
    }

    protected abstract void tick(long delta);

}