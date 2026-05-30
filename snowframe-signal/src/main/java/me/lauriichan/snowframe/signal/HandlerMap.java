package me.lauriichan.snowframe.signal;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

//TODO: WIP
final class HandlerMap {

    private final Object2ObjectOpenHashMap<HandlerPriority, ObjectArrayList<SignalContainer>> map = new Object2ObjectOpenHashMap<>();
    
    public void put(SignalContainer container) {
        
    }

}
