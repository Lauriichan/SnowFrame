package me.lauriichan.snowframe.signal;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class SignalManager {

    private final ObjectArrayList<SignalContainer> containers = new ObjectArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ISimpleLogger logger;

    public SignalManager(ISimpleLogger logger) {
        this.logger = logger;
    }

    public final ISimpleLogger logger() {
        return logger;
    }

    public final SignalContainer register(ISignalHandler handler) {
        lock.readLock().lock();
        try {
            for (int index = 0; index < containers.size(); index++) {
                final SignalContainer container = containers.get(index);
                if (Objects.equals(container.handler(), handler)) {
                    return container;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        SignalContainer container = handler.newContainer();
        lock.writeLock().lock();
        try {
            containers.add(container);
        } finally {
            lock.writeLock().unlock();
        }
        return container;
    }

    public final boolean unregister(final SignalContainer container) {
        lock.readLock().lock();
        try {
            if (!containers.contains(container)) {
                return false;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            return containers.remove(container);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final <S extends ISignal> boolean call(S signal) {
        SignalContext<S> context = new SignalContext<>(signal);
        call(context);
        return context.isCancelled();
    }

    public final <S extends ISignal> boolean call(S signal, Consumer<SignalContext<S>> contextSetup) {
        SignalContext<S> context = new SignalContext<>(signal);
        if (contextSetup != null) {
            contextSetup.accept(context);
        }
        call(context);
        return context.isCancelled();
    }

    public final <S extends ISignal> void call(SignalContext<S> context) {
        SignalContainer[] containers;
        lock.readLock().lock();
        try {
            if (this.containers.isEmpty()) {
                return;
            }
            containers = this.containers.toArray(new SignalContainer[this.containers.size()]);
        } finally {
            lock.readLock().unlock();
        }
        for (SignalContainer current : containers) {
            if (context.isCancelled() && !current.allowsCancelled()) {
                continue;
            }
            current.handleSignal(this, context);
        }
    }

}
