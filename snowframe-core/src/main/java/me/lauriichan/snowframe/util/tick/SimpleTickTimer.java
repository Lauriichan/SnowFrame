package me.lauriichan.snowframe.util.tick;

import java.util.concurrent.ThreadFactory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SimpleTickTimer extends AbstractTickTimer {

    public static interface ITickable {

        void tick(long delta);

    }

    private final ObjectArrayList<ITickable> tickables = new ObjectArrayList<>();
    private final ThreadFactory factory;
    
    public SimpleTickTimer() {
        this(Thread.ofVirtual().factory());
    }
    
    public SimpleTickTimer(final ThreadFactory factory) {
        this.factory = factory;
    }
    
    public SimpleTickTimer add(ITickable tickable) {
        if (tickable == null || tickables.contains(tickable)) {
            return this;
        }
        tickables.add(tickable);
        return this;
    }
    
    public boolean remove(ITickable tickable) {
        return tickables.remove(tickable);
    }
    
    public SimpleTickTimer clear() {
        tickables.clear();
        return this;
    }

    @Override
    protected Thread createThread(Runnable runnable) {
        return factory.newThread(runnable);
    }

    @Override
    protected void tick(long delta) {
        if (this.tickables.isEmpty()) {
            return;
        }
        ITickable[] tickables = this.tickables.toArray(ITickable[]::new);
        for (ITickable tickable : tickables) {
            tickable.tick(delta);
        }
    }

}