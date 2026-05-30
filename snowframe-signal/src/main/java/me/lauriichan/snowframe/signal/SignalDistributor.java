package me.lauriichan.snowframe.signal;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;

public class SignalDistributor {
    
    private final ObjectArrayList<SignalManager> managers = new ObjectArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ISimpleLogger logger;

    public SignalDistributor(ISimpleLogger logger) {
        this.logger = logger;
    }

    public final ISimpleLogger logger() {
        return logger;
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
        SignalManager[] managers;
        lock.readLock().lock();
        try {
            if (this.managers.isEmpty()) {
                return;
            }
            managers = this.managers.toArray(SignalManager[]::new);
        } finally {
            lock.readLock().unlock();
        }
        Object2ObjectOpenHashMap<HandlerPriority, ObjectArrayList<SignalManager>> a;
        for (SignalManager manager : managers) {
            
        }
    }

}
