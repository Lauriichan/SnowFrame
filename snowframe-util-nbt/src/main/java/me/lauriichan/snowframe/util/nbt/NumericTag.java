package me.lauriichan.snowframe.util.nbt;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class NumericTag<T extends NumericTag<T>> extends Tag<T> {
    
    public abstract Number asNumber();
    
    public boolean asBoolean() {
        return asByte() != 0;
    }
    
    public byte asByte() {
        return asNumber().byteValue();
    }
    
    public short asShort() {
        return asNumber().shortValue();
    }
    
    public int asInt() {
        return asNumber().intValue();
    }
    
    public long asLong() {
        return asNumber().longValue();
    }
    
    public float asFloat() {
        return asNumber().floatValue();
    }
    
    public double asDouble() {
        return asNumber().doubleValue();
    }

    public BigInteger asBigInt() {
        return BigInteger.valueOf(asLong());
    }
    
    public BigDecimal asBigDecimal() {
        return BigDecimal.valueOf(asDouble());
    }

}
