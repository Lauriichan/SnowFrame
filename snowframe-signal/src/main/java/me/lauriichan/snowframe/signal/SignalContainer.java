package me.lauriichan.snowframe.signal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public final class SignalContainer {

    private final ISignalHandler handler;

    private final ObjectList<SignalReceiver<?>> receivers;

    private final boolean allowsCancelled;

    public SignalContainer(final ISignalHandler handler, final SignalReceiver<?>[] receivers) {
        final ObjectArrayList<SignalReceiver<?>> receiverList = new ObjectArrayList<>();
        boolean allowsCancelled = false;
        for (final SignalReceiver<?> receiver : receivers) {
            receiverList.add(receiver);
            if (receiver.allowsCancelled()) {
                allowsCancelled = true;
            }
        }
        this.handler = handler;
        this.receivers = ObjectLists.unmodifiable(receiverList);
        this.allowsCancelled = allowsCancelled;
    }

    public ISignalHandler handler() {
        return handler;
    }

    public boolean allowsCancelled() {
        return allowsCancelled;
    }

    final void handleSignal(SignalManager manager, SignalContext<?> context) {
        for (final SignalReceiver<?> receiver : receivers) {
            if (!receiver.isSignalSuitable(context.signalType()) || context.isCancelled() && !receiver.allowsCancelled()) {
                continue;
            }
            handle(manager, receiver, context);
        }
    }

    @SuppressWarnings("unchecked")
    private <S extends ISignal> void handle(SignalManager manager, SignalReceiver<S> receiver, SignalContext<?> context) {
        receiver.handle(manager, (SignalContext<S>) context);
    }

}
