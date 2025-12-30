package me.lauriichan.snowframe.signal;

import me.lauriichan.snowframe.util.attribute.Attributable;

public final class SignalContext<S extends ISignal> extends Attributable {

    private final S signal;
    private final Class<S> signalType;
    private final boolean cancelable;
    
    private volatile boolean cancelled = false;
    
    @SuppressWarnings("unchecked")
    public SignalContext(S signal) {
        this.signal = signal;
        this.signalType = (Class<S>) signal.getClass();
        this.cancelable = ICancelable.class.isAssignableFrom(signalType);
    }

    public final S signal() {
        return signal;
    }
    
    public final Class<S> signalType() {
        return signalType;
    }

    public final boolean isCancelable() {
        return cancelable;
    }
    
    public final boolean isCancelled() {
        return cancelled;
    }
    
    public final void setCancelled(boolean cancelled) {
        if (!cancelable) {
            return;
        }
        this.cancelled = cancelled;
    }

}
