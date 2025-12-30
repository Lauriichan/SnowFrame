package me.lauriichan.snowframe.signal;

public final class SignalReceiver<S extends ISignal> {

    private final Class<S> signalType;
    private final ISignalFunction<S> receiver;

    private final boolean allowCancelled;

    public SignalReceiver(Class<S> signalType, ISignalFunction<S> receiver) {
        this(signalType, receiver, true);
    }

    public SignalReceiver(Class<S> signalType, ISignalFunction<S> receiver, boolean allowCancelled) {
        this.signalType = signalType;
        this.receiver = receiver;
        this.allowCancelled = allowCancelled;
    }

    public final boolean isSignalSuitable(Class<? extends ISignal> signalType) {
        return this.signalType.isAssignableFrom(signalType);
    }

    public final boolean allowsCancelled() {
        return allowCancelled;
    }

    final void handle(SignalManager manager, SignalContext<S> context) {
        try {
            receiver.onSignal(context);
        } catch (Throwable e) {
            manager.logger().error("Failed to run signal handler", e);
        }
    }

}
