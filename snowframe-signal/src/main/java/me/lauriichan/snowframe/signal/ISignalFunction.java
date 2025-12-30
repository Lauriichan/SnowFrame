package me.lauriichan.snowframe.signal;

@FunctionalInterface
public interface ISignalFunction<S extends ISignal> {
    
    void onSignal(SignalContext<S> context) throws Throwable;
    
}
