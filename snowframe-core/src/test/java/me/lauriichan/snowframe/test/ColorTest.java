package me.lauriichan.snowframe.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import me.lauriichan.snowframe.util.color.HexParser;
import me.lauriichan.snowframe.util.color.SimpleColor;

public class ColorTest {

    @Test
    public void testHexColor() {
        String originalHex = "#F96EA3";
        SimpleColor color = SimpleColor.sRGB(originalHex);
        assertEquals(originalHex, color.asHex().toUpperCase());
    }
    
    @Test
    public void testHexParser() {
        String hex = "F96EA3";
        int value = HexParser.parseHexInt(hex);
        int javaValue = Integer.parseInt(hex, 16);
        assertEquals(javaValue, value);
        assertEquals(0xF96EA3, value);
    }
    
}
