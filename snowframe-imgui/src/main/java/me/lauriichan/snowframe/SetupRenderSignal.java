package me.lauriichan.snowframe;

import me.lauriichan.snowframe.signal.ISignal;
import me.lauriichan.snowframe.util.color.SimpleColor;
import me.lauriichan.snowframe.util.tick.BlockingTicker;

public final class SetupRenderSignal implements ISignal {

    private final BlockingTicker ticker;
    private final SimpleColor background;

    private final long windowPointer;

    public SetupRenderSignal(BlockingTicker ticker, SimpleColor background, long windowPointer) {
        this.ticker = ticker;
        this.background = background;
        this.windowPointer = windowPointer;
    }

    public BlockingTicker ticker() {
        return ticker;
    }

    public SimpleColor background() {
        return background;
    }

    public long windowPointer() {
        return windowPointer;
    }

}
